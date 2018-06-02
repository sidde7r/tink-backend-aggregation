package se.tink.backend.system.controllers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.product.targeting.TargetProductsRunnableFactory;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.rpc.DeleteAccountRequest;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * If account is not imported it should be marked as closed and excluded. If user has modified
 * {@link Account#excluded} we should not save it anymore; when it's re-enabled, it should be
 * saved unless closed.
 */
public class AccountController {

    private static final LogUtils log = new LogUtils(AccountController.class);
    private static final MetricId ACCOUNT_CLOSED_CHANGED = MetricId.newId("account_closed_changed");
    private static final MetricId ACCOUNT_EXCLUDED_CHANGED = MetricId.newId("account_excluded_changed");

    private final AccountRepository accountRepository;
    private final AccountDao accountDao;
    private final FirehoseQueueProducer firehoseQueueProducer;
    private final UserRepository userRepository;
    private final TargetProductsRunnableFactory targetProductsRunnableFactory;
    private final ListenableThreadPoolExecutor<Runnable> executorService;
    private final CredentialsRepository credentialsRepository;
    private final MetricRegistry metricRegistry;

    @Inject
    public AccountController(AccountRepository accountRepository,
            AccountDao accountDao,
            FirehoseQueueProducer firehoseQueueProducer,
            UserRepository userRepository,
            TargetProductsRunnableFactory targetProductsRunnableFactory,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executorService,
            CredentialsRepository credentialsRepository,
            MetricRegistry metricRegistry) {
        this.accountRepository = accountRepository;
        this.accountDao = accountDao;
        this.firehoseQueueProducer = firehoseQueueProducer;
        this.userRepository = userRepository;
        this.targetProductsRunnableFactory = targetProductsRunnableFactory;
        this.executorService = executorService;
        this.credentialsRepository = credentialsRepository;
        this.metricRegistry = metricRegistry;
    }

    public void process(String userId, String credentialsId, List<String> importedAccountIds) {
        Credentials credentials = credentialsRepository.findOne(credentialsId);
        List<Account> accountsToSave = prepareToSave(
                accountRepository.findByUserIdAndCredentialsId(userId, credentialsId),
                importedAccountIds, credentials.getProviderName());

        if (!accountsToSave.isEmpty()) {
            firehoseQueueProducer.sendAccountsMessage(userId, FirehoseMessage.Type.UPDATE, accountsToSave);

            accountRepository.save(accountsToSave);

            User user = userRepository.findOne(userId);
            Runnable runnable = targetProductsRunnableFactory.createRunnable(user);

            if (runnable != null) {
                executorService.execute(runnable);
            }
        }
    }

    List<Account> prepareToSave(List<Account> existingAccounts, List<String> importedAccountIds, String providerName) {
        return existingAccounts.stream().flatMap((Account account) -> {
            boolean wasImported = importedAccountIds.contains(account.getId());
            boolean oldIsClosed = account.isClosed();
            boolean oldIsExcluded = account.isExcluded();

            if (!account.isClosed() && !wasImported) {
                // Open -> Closed
                metricRegistry.meter(ACCOUNT_CLOSED_CHANGED.label("provider", providerName).label("state", "closed"))
                        .inc();
                account.setClosed(true);
                account.setBalance(0);
            } else if (account.isClosed() && wasImported) {
                // Closed -> Open
                metricRegistry.meter(ACCOUNT_CLOSED_CHANGED.label("provider", providerName).label("state", "open"))
                        .inc();
                // Increased log level as opening a closed account could be a sign of a flaky provider.
                log.warn(account.getUserId(), account.getCredentialsId(), "Closed account opened.");
                account.setClosed(false);
            }

            // Exclusion will only be toggled if it hasn't been modified by the user.
            if (!account.isUserModifiedExcluded()) {
                if (account.isExcluded() && wasImported) {
                    // Include account if it's imported again.
                    metricRegistry
                            .meter(ACCOUNT_EXCLUDED_CHANGED.label("provider", providerName).label("state", "included"))
                            .inc();
                    account.setExcluded(false);
                } else if (!account.isExcluded() && !wasImported && account.getType() == AccountTypes.LOAN) {
                    // Exclude loan accounts that wasn't imported. Other account types are not excluded
                    // as that would remove transactions for the users.
                    metricRegistry
                            .meter(ACCOUNT_EXCLUDED_CHANGED.label("provider", providerName).label("state", "excluded"))
                            .inc();
                    account.setExcluded(true);
                }
            }

            // Only save on modifications.
            if (account.isClosed() != oldIsClosed || account.isExcluded() != oldIsExcluded) {
                return Stream.of(account);
            } else {
                return Stream.empty();
            }

        }).collect(Collectors.toList());
    }

    public void deleteAccounts(DeleteAccountRequest deleteAccountRequest) {
        String userId = deleteAccountRequest.getUserId();
        String credentialsId = deleteAccountRequest.getCredentialsId();
        String accountId = deleteAccountRequest.getAccountId();

        Account account = accountRepository.findOne(accountId);

        if (account == null) {
            log.info(userId, credentialsId, "Did not find any account to delete with ID: " + accountId);
            return;
        }
        accountDao.delete(account);
    }
}
