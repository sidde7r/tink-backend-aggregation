package se.tink.backend.abnamro.migration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.CreateCredentialsRequest;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.controllers.DeleteController;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;

/**
 * This is a utility class that will migrate ABN AMRO credentials from the old structure to a new one.
 * Old structure:
 * - nl-abnamro-ics-abstract => Used for pulling transactions from ICS. This provider has the status `ENABLED` and
 * transactions are fetched/pulled  every night.
 * - nl-abnamro-abstract => Used when ABN is sending transactions to the connector. This provider is DISABLED.
 * <p>
 * New structure:
 * - nl-abnamro => The new provider that is used both for aggregation and when transactions are pushed to the connector.
 * <p>
 * This migrator will merge together the two old credentials into one new credential.
 */
public class AbnAmroCredentialsMigrator {
    private static final LogUtils log = new LogUtils(AbnAmroCredentialsMigrator.class);

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final CredentialsRepository credentialsRepository;
    private final ProviderRepository providerRepository;
    private final AggregationServiceFactory aggregationServiceFactory;
    private final TransactionDao transactionDao;
    private final AccountDao accountDao;
    private final DeleteController deleteController;
    private final boolean isProvidersOnAggregation;

    @Inject
    public AbnAmroCredentialsMigrator(@Named("useAggregationController") boolean isUseAggregationController,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            CredentialsRepository credentialsRepository,
            ProviderRepository providerRepository,
            AccountDao accountDao,
            TransactionDao transactionDao,
            AggregationServiceFactory aggregationServiceFactory,
            DeleteController deleteController,
            @Named("isProvidersOnAggregation") boolean isProvidersOnAggregation) {
        this.isUseAggregationController = isUseAggregationController;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;
        this.credentialsRepository = credentialsRepository;
        this.providerRepository = providerRepository;
        this.aggregationServiceFactory = aggregationServiceFactory;
        this.transactionDao = transactionDao;
        this.accountDao = accountDao;
        this.deleteController = deleteController;
        this.isProvidersOnAggregation = isProvidersOnAggregation;
    }

    /**
     * ABN AMRO users had one or two credentials before the migration. One for the normal ABN AMRO connection and one
     * for ICS credit cards. This method will merge these two credentials together.
     */
    public void migrate(User user) {
        Preconditions.checkNotNull(user, "User must not be null");

        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());

        if (credentials.size() == 0) {
            log.info(user.getId(), "User did not have any credentials. Skipping.");
            return;
        }

        if (isMigrated(credentials)) {
            log.info(user.getId(), "Credentials already migrated. Skipping.");
            return;
        }

        // We expect only one ABN AMRO credential and optionally one ABN AMRO ICS credential for every user.
        Map<String, Credentials> credentialsByProvider = Maps.uniqueIndex(credentials, Credentials::getProviderName);

        Credentials abnCredential = credentialsByProvider.get(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME);
        Credentials icsCredential = credentialsByProvider.get(AbnAmroUtils.ABN_AMRO_ICS_PROVIDER_NAME);

        if (abnCredential == null) {
            throw new RuntimeException(
                    String.format("User did not have an ABN AMRO credentials. (UserId = '%s')", user.getId()));
        }

        abnCredential = migrateAbnAmroCredentials(user, abnCredential);

        if (icsCredential != null) {
            mergeCredentials(user, icsCredential, abnCredential);
        }

        log.info(user.getId(), "ABN AMRO credential(s) migrated.");
    }

    private Credentials migrateAbnAmroCredentials(User user, Credentials credentials) {
        final Provider provider;
        if (isProvidersOnAggregation) {
            provider = aggregationControllerCommonClient.getProviderByName(credentials.getProviderName());
        } else {
            provider = providerRepository.findByName(credentials.getProviderName());
        }

        // Change to new provider name
        credentials.setProviderName(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME_V2);

        String bcNumber = credentials.getField(AbnAmroUtils.BC_NUMBER_FIELD_NAME);

        // Put the bcNumber of the payload
        credentials.setPayload(bcNumber);

        // Reset the fields, it is not used on the new provider
        credentials.setFieldsSerialized(null);

        credentialsRepository.save(credentials);

        if (isUseAggregationController && !Objects.equals(CredentialsTypes.FRAUD, credentials.getType())) {
            se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CreateCredentialsRequest createCredentialsRequest =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CreateCredentialsRequest(
                            user, provider, credentials);

            credentials = aggregationControllerCommonClient.createCredentials(createCredentialsRequest);
        } else {
            CreateCredentialsRequest request = new CreateCredentialsRequest(CoreUserMapper.toAggregationUser(user),
                    CoreProviderMapper.toAggregationProvider(provider),
                    CoreCredentialsMapper.toAggregationCredentials(credentials));

            // Create the credential in aggregation
            se.tink.backend.aggregation.rpc.Credentials aggregationCredentials = aggregationServiceFactory
                    .getAggregationService().createCredentials(request);

            // Save the credential since we have received the secret key
            credentials = CoreCredentialsMapper.fromAggregationCredentials(aggregationCredentials);
        }

        credentialsRepository.save(credentials);

        return credentials;
    }

    /**
     * Merge two credentials together by moving all transactions and accounts from the source credentials to the
     * destination credentials.
     */
    private void mergeCredentials(User user, Credentials source, Credentials destination) {
        List<Account> accounts = accountDao.findByUserIdAndCredentialsId(source.getUserId(), source.getId());

        // 1. Migrate all the accounts to the destination credentials
        accounts.forEach(a -> a.setCredentialsId(destination.getId()));
        accountDao.save(accounts);

        // 2. Find all the transactions on the source credentials
        List<Transaction> transactions = transactionDao.findAllByUser(user).stream().filter(t ->
                Objects.equals(t.getCredentialsId(), source.getId())).collect(Collectors.toList());

        // 2. Migrate all the transactions to the destination credential
        transactions.forEach(t -> t.setCredentialsId(destination.getId()));

        transactionDao.saveAndIndex(user, transactions, true);

        // This call will also recalculate statistics
        deleteController.deleteCredentials(user, source.getId(), true, Optional.empty());
    }

    /**
     * Check if the user already is migrated. User is considered migrated of at least on credential is for the provider
     * nl-abnamro.
     */
    private boolean isMigrated(List<Credentials> credentials) {
        return credentials.stream()
                .anyMatch(c -> Objects.equals(c.getProviderName(), AbnAmroUtils.ABN_AMRO_PROVIDER_NAME_V2));
    }
}
