package se.tink.backend.main.controllers;

import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountDetails;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.main.controllers.abnamro.AbnAmroCreditCardController;
import se.tink.backend.rpc.UpdateAccountRequest;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.SequenceTimer;
import se.tink.libraries.metrics.SequenceTimers;

public class AccountServiceController {
    private static class UpdateAccountTimers {
        private static final String FIND_ACCOUNT = "find_account";
        private static final String UPDATE_ACCOUNT_PROPERTIES = "update_account_properties";
        private static final String SAVE_ACCOUNT = "save_account";
        private static final String PURGE_CACHE = "purge_cache";
        private static final String GENERATE_STATISTICS = "generate_statistics";
        private static final String GENERATE_SIMPLE_STATISTICS = "generate_simple_statistics";
        private static final String UPDATE_CONTEXT = "update_context";
        private static final String UPDATE_ABN_USER = "update_abn_user";
        private static final String FIND_PROVIDERS = "find_providers";
        private static final String GET_ACCOUNT_PROPERTIES = "get_account_properties";
    }

    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;
    private final LoanDataRepository loanDataRepository;
    private final UserRepository userRepository;
    private final UserStateRepository userStateRepository;
    private final CacheClient cacheClient;
    private final SystemServiceFactory systemServiceFactory;
    private final FirehoseQueueProducer firehoseQueueProducer;

    private final Supplier<ProviderImageMap> providerImageMapSupplier;
    private final Cluster cluster;

    private final AbnAmroCreditCardController abnAmroCreditCardController;

    private final SequenceTimer updateAccountSequenceTimer;

    @Inject
    public AccountServiceController(CacheClient cacheClient, SystemServiceFactory systemServiceFactory,
            AccountRepository accountRepository, CredentialsRepository credentialsRepository,
            LoanDataRepository loanDataRepository, UserStateRepository userStateRepository,
            UserRepository userRepository,
            FirehoseQueueProducer firehoseQueueProducer,
            Supplier<ProviderImageMap> providerImageMapSupplier,
            Cluster cluster, AbnAmroCreditCardController abnAmroCreditCardController,
            MetricRegistry metricRegistry) {

        this.cacheClient = cacheClient;
        this.firehoseQueueProducer = firehoseQueueProducer;
        this.cluster = cluster;
        this.systemServiceFactory = systemServiceFactory;

        this.accountRepository = accountRepository;
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
        this.loanDataRepository = loanDataRepository;
        this.userStateRepository = userStateRepository;
        this.providerImageMapSupplier = providerImageMapSupplier;

        this.abnAmroCreditCardController = abnAmroCreditCardController;

        updateAccountSequenceTimer = new SequenceTimer(AccountServiceController.class, metricRegistry,
                SequenceTimers.UPDATE_ACCOUNT);
    }

    private void purgeSuggestFromCache(String userId) {
        cacheClient.delete(CacheScope.SUGGEST_TRANSACTIONS_RESPONSE_BY_USERID, userId);
    }

    public List<Account> list(String userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        Map<String, String> providerNamesByCredentials = credentialsRepository.findAllIdsAndProviderNamesByUserId(
                userId);

        accounts.forEach(account -> {
            account.setDetails(getDetails(account).orElse(null));
            account.setImages(getAccountImages(account, providerNamesByCredentials));
        });

        return accounts;
    }

    private Optional<AccountDetails> getDetails(Account account) {
        switch (account.getType()) {
        case MORTGAGE:
        case LOAN:
            return Optional.ofNullable(loanDataRepository.findMostRecentOneByAccountId(account.getId()))
                    .map(AccountDetails::new);
        default:
            return Optional.empty();
        }
    }

    private ImageUrls getAccountImages(Account account, Map<String, String> providerNamesByCredentials) {
        String providerName = providerNamesByCredentials.get(account.getCredentialsId());
        return providerImageMapSupplier.get().getImagesForAccount(providerName, account);
    }

    public Account update(String userId, String id, final Account account) {
        return update(userId, id,
                new UpdateAccountRequest(account.getAccountNumber(), account.getName(), account.getType(),
                        account.isFavored(), account.isExcluded(), account.getOwnership()));
    }

    public Account update(String userId, String id, final UpdateAccountRequest updateRequest) {
        SequenceTimer.Context sequenceTimerContext = updateAccountSequenceTimer.time();
        // Find account
        sequenceTimerContext.mark(UpdateAccountTimers.FIND_ACCOUNT);
        Account existingAccount = accountRepository.findOne(id);

        if (existingAccount == null) {
            throw new NoSuchElementException();
        }

        if (!Objects.equal(existingAccount.getUserId(), userId)) {
            throw new IllegalArgumentException();
        }

        boolean modifiedExcluded = isModifiedField(updateRequest.getExcluded(), existingAccount.isExcluded());
        boolean modifiedOwnership = isModifiedField(updateRequest.getOwnership(), existingAccount.getOwnership());

        // updateAccount
        sequenceTimerContext.mark(UpdateAccountTimers.UPDATE_ACCOUNT_PROPERTIES);
        updateAccountProperties(existingAccount, updateRequest);

        if (!existingAccount.isUserModifiedExcluded()) {
            // Only set user modified if not set earlier.
            existingAccount.setUserModifiedExcluded(true);
        }

        // save user account
        sequenceTimerContext.mark(UpdateAccountTimers.SAVE_ACCOUNT);
        Account finalAccount = accountRepository.save(existingAccount);

        // Exclude transactions based on the state of the account.
        sequenceTimerContext.mark(UpdateAccountTimers.GENERATE_STATISTICS);
        if (modifiedExcluded) {
            purgeSuggestFromCache(userId);
            systemServiceFactory.getProcessService()
                    .generateStatisticsAndActivitiesWithoutNotifications(userId, StatisticMode.FULL);
        } else if (modifiedOwnership) {
            systemServiceFactory.getProcessService()
                    .generateStatisticsAndActivitiesWithoutNotifications(userId, StatisticMode.SIMPLE);
        }

        sequenceTimerContext.mark(UpdateAccountTimers.UPDATE_CONTEXT);
        userStateRepository.updateContextTimestampByUserId(userId, cacheClient);

        if (Objects.equal(cluster, Cluster.ABNAMRO)) {
            sequenceTimerContext.mark(UpdateAccountTimers.UPDATE_ABN_USER);
            User user = userRepository.findOne(userId);

            abnAmroCreditCardController.accountUpdated(user, finalAccount);
        }
        sequenceTimerContext.mark(UpdateAccountTimers.FIND_PROVIDERS);
        Map<String, String> providerNamesByCredentials = credentialsRepository.findAllIdsAndProviderNamesByUserId(
                userId);

        sequenceTimerContext.mark(UpdateAccountTimers.GET_ACCOUNT_PROPERTIES);
        finalAccount.setDetails(getDetails(finalAccount).orElse(null));
        finalAccount.setImages(getAccountImages(finalAccount, providerNamesByCredentials));
        sequenceTimerContext.stop();

        firehoseQueueProducer.sendAccountMessage(userId, FirehoseMessage.Type.UPDATE, finalAccount);
        return finalAccount;
    }

    private void updateAccountProperties(Account account, UpdateAccountRequest updateAccount) {
        if (isModifiedField(updateAccount.getAccountNumber(), account.getAccountNumber())) {
            account.setAccountNumber(updateAccount.getAccountNumber());
        }

        if (isModifiedField(updateAccount.getName(), account.getName())) {
            account.setName(updateAccount.getName());
            account.setUserModifiedName(true);
        }

        if (isModifiedField(updateAccount.getType(), account.getType())) {
            account.setType(updateAccount.getType());
            account.setUserModifiedType(true);
        }

        if (isModifiedField(updateAccount.getFavored(), account.isFavored())) {
            account.setFavored(updateAccount.getFavored());

            // Set a flag on the user that user has changed favorite accounts,
            // which in turn means that we should not automatically try to determine
            // his favorite accounts every time we calculate statistics.
            UserState userState = userStateRepository.findOne(account.getUserId());
            userState.setHaveManuallyFavoredAccount(true);
            userStateRepository.save(userState);
        }

        if (isModifiedField(updateAccount.getExcluded(), account.isExcluded())) {
            account.setExcluded(updateAccount.getExcluded());
        }

        if (account.isExcluded()) {
            account.setFavored(false);
        }

        if (isModifiedField(updateAccount.getOwnership(), account.getOwnership())) {
            account.setOwnership(updateAccount.getOwnership());
        }
    }

    private <T> boolean isModifiedField(T newField, T oldField) {
        return newField != null && !Objects.equal(newField, oldField);
    }
}
