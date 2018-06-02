package se.tink.backend.system.cli.abnamro;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import org.joda.time.DateTime;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedAccountRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroSubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.AbnAmroBufferedAccount;
import se.tink.backend.core.AbnAmroSubscription;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.cleanup.CleanupDuplicateTransactionsCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.abnamro.client.IBSubscriptionClient;
import se.tink.libraries.abnamro.client.rpc.ResyncRequest;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.metrics.MetricRegistry;

public class ResyncTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(ResyncTransactionsCommand.class);

    private static final double PERMITS_PER_SECOND = 1000;
    private static final int DEFAULT_PERMITS = 5000; // 1000 / 5000 = 0.2 requests per second

    private static final int CREDENTIALS_UPDATED_RETRIES = 12;
    private static final int CREDENTIALS_UPDATED_WAIT_SECONDS = 10;

    private static final ImmutableSet<String> PROVIDERS_TO_RESYNC = ImmutableSet
            .of(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME, AbnAmroUtils.ABN_AMRO_PROVIDER_NAME_V2);

    private AbnAmroBufferedAccountRepository abnAmroBufferedAccountRepository;
    private AbnAmroSubscriptionRepository abnAmroSubscriptionRepository;
    private CredentialsRepository credentialsRepository;
    private TransactionDao transactionDao;
    private AccountRepository accountRepository;
    private UserRepository userRepository;
    private ServiceContext serviceContext;

    public ResyncTransactionsCommand() {
        super("resync-transactions", "Re-sync transactions for ABN AMRO users.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext)
            throws Exception {

        log.info("Re-sync historical data for ABN AMRO users.");

        if (!Objects.equal(configuration.getCluster(), Cluster.ABNAMRO)) {
            log.error("This command is only enabled in the ABN AMRO cluster.");
            return;
        }

        final CleanDuplicatesConfig cleanDuplicatesConfig = new CleanDuplicatesConfig();
        cleanDuplicatesConfig.setCleanDuplicates(Boolean.getBoolean("cleanDuplicates"));
        cleanDuplicatesConfig.setDryRun(Boolean.getBoolean("cleanDuplicatesDryRun"));
        cleanDuplicatesConfig.setForceDeleteOldest(Boolean.getBoolean("cleanDuplicatesForceDeleteOldest"));
        cleanDuplicatesConfig.setOnlyCompareExternalId(Boolean.getBoolean("cleanDuplicatesOnlyCompareExternalId"));
        cleanDuplicatesConfig.setSleepSeconds(Integer.getInteger("cleanDuplicatesSleepSeconds",
                CREDENTIALS_UPDATED_WAIT_SECONDS));

        this.serviceContext = serviceContext;
        userRepository = serviceContext.getRepository(UserRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        abnAmroBufferedAccountRepository = serviceContext.getRepository(AbnAmroBufferedAccountRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        abnAmroSubscriptionRepository = serviceContext.getRepository(AbnAmroSubscriptionRepository.class);

        final RateLimiter rateLimiter = RateLimiter.create(PERMITS_PER_SECOND);
        final int permits = Integer.getInteger("permits", DEFAULT_PERMITS);
        final AtomicInteger userCount = new AtomicInteger();

        final Optional<DateTime> fromDate = getDateSystemProperty("fromDate");
        final Optional<DateTime> endDate = getDateSystemProperty("endDate");
        final boolean countTransactionChanges = Boolean.getBoolean("countTransactionChanges");

        // Either sync by bc number or by users

        String bcNumberInput = System.getProperty("bcnumbers");

        if (!Strings.isNullOrEmpty(bcNumberInput)) {

            ImmutableList<String> bcNumbers = ImmutableList.copyOf(bcNumberInput.split(";"));

            resync(bcNumbers, rateLimiter, permits, userCount, cleanDuplicatesConfig, fromDate, endDate,
                    countTransactionChanges);

        } else {

            userRepository.streamAll()
                    .compose(new CommandLineInterfaceUserTraverser(10))
                    .forEach(user -> {
                        try {
                            resync(user, rateLimiter, permits, userCount, cleanDuplicatesConfig, fromDate, endDate,
                                    countTransactionChanges);
                        } catch (Exception e) {
                            log.error(user.getId(), "Resync failed.", e);
                        }
                    });
        }
    }

    private Optional<DateTime> getDateSystemProperty(String property) {
        String inputDate = System.getProperty(property);

        if (Strings.isNullOrEmpty(inputDate)) {
            return Optional.empty();
        }

        return Optional.of(DateTime.parse(inputDate));
    }

    /**
     * Resync users based on a list of "bc numbers".
     */
    private void resync(List<String> bcNumbers, RateLimiter rateLimiter, int permits, AtomicInteger userCount,
            CleanDuplicatesConfig cleanDuplicatesConfig, Optional<DateTime> fromDate, Optional<DateTime> endDate,
            boolean countTransactionChanges) {

        for (String bcNumber : bcNumbers) {

            User user = userRepository.findOneByUsername(AbnAmroLegacyUserUtils.getUsername(bcNumber));

            if (user == null) {
                log.error(String.format("User was not found in database. (BcNumber = '%s')", bcNumber));
            } else {
                try {
                    resync(user, rateLimiter, permits, userCount, cleanDuplicatesConfig, fromDate, endDate,
                            countTransactionChanges);
                } catch (Exception e) {
                    log.error(user.getId(), "Resync failed.", e);
                }
            }
        }
    }

    private void resync(User user, RateLimiter rateLimiter, int permits, AtomicInteger userCount,
            CleanDuplicatesConfig cleanDuplicatesConfig, Optional<DateTime> fromDate, Optional<DateTime> endDate,
            boolean countTransactionChanges) {

        if (user.getFlags().contains(FeatureFlags.TINK_TEST_ACCOUNT)) {
            log.warn(user.getId(), String.format("Don't re-sync test accounts (with the feature flag %s). Skipping.",
                    FeatureFlags.TINK_TEST_ACCOUNT));
            return;
        }

        // Only do this check if the user is on Grip 2.0
        if (AbnAmroLegacyUserUtils.isValidUsername(user.getUsername())) {
            // Don't resync users that hasn't activated their account. Typically old pilot users.
            AbnAmroSubscription subscription = abnAmroSubscriptionRepository.findOneByUserId(user.getId());

            if (subscription == null || !subscription.isActivated()) {
                log.info(user.getId(), "Not re-syncing user with a subscription that hasn't been activated.");
                return;
            }
        }

        final Optional<Credentials> optionalCredentials = getAbnAmroCredentials(user);

        if (!optionalCredentials.isPresent()) {
            log.warn(user.getId(), "No valid ABN AMRO credentials. Skipping.");
            return;
        }

        final Credentials credentials = optionalCredentials.get();

        ImmutableList<Long> bbanNumbers = FluentIterable
                .from(accountRepository.findByUserId(user.getId()))
                .filter(account -> Objects.equal(account.getCredentialsId(), credentials.getId()) &&
                        !AbnAmroUtils.isAccountRejected(account))
                .transform(account -> Long.valueOf(account.getBankId()))
                .toList();

        if (bbanNumbers.isEmpty()) {
            log.warn(user.getId(), "No valid ABN AMRO accounts. Skipping.");
            return;
        }

        updateCredentialsStatus(credentials, CredentialsStatus.UPDATING);

        double timeSlept = rateLimiter.acquire(permits);
        log.info(String.format("Re-sync userId=%s (slept for %.0fms).", user.getId(), timeSlept * 1000));

        // Count number of transactions before the resync
        if (countTransactionChanges) {
            log.info("Number of transactions per account (before resync):");
            countNumberOfTransactions(user);
        }

        ResyncRequest resyncRequest = new ResyncRequest();
        resyncRequest.setAccounts(bbanNumbers);

        try {

            AbnAmroConfiguration configuration = serviceContext.getConfiguration().getAbnAmro();

            IBSubscriptionClient client = new IBSubscriptionClient(configuration, new MetricRegistry());

            boolean success;

            createBufferedAccounts(credentials, bbanNumbers);

            if (fromDate.isPresent()) {
                success = client.resync(bbanNumbers, fromDate.get(), endDate.orElse(DateTime.now()));
            } else {
                success = client.resync(bbanNumbers);
            }

            if (success) {
                log.info(String.format("Users re-synced: %s.", userCount.incrementAndGet()));
            } else {
                log.warn("Unable to do resync.");
                updateCredentialsStatus(credentials, CredentialsStatus.UPDATED);
            }
        } catch (Exception e) {
            log.error(user.getId(), "Unable to create a PFM client to invoke re-sync.", e);
        }

        // Count number of transactions after the resync
        if (countTransactionChanges) {
            waitUntilCredentialsIsUpdated(credentials, CREDENTIALS_UPDATED_WAIT_SECONDS);
            log.info("Number of transactions per account (after resync):");
            countNumberOfTransactions(user);
        }

        if (cleanDuplicatesConfig.shouldCleanDuplicates()) {
            log.info("Cleaning duplicates.");
            cleanDuplicates(user, credentials, cleanDuplicatesConfig);

            // Count number of transactions after the clean
            if (countTransactionChanges) {
                waitUntilCredentialsIsUpdated(credentials, CREDENTIALS_UPDATED_WAIT_SECONDS);
                log.info("Number of transactions per account (after cleaning):");
                countNumberOfTransactions(user);
            }
        }
    }

    private void createBufferedAccounts(Credentials credentials, List<Long> accountNumbers) {

        List<AbnAmroBufferedAccount> bufferedAccounts = Lists.newArrayList();

        for (Long accountNumber : accountNumbers) {
            AbnAmroBufferedAccount bufferedAccount = new AbnAmroBufferedAccount();
            bufferedAccount.setCredentialsId(credentials.getId());
            bufferedAccount.setAccountNumber(accountNumber);
            bufferedAccount.setTransactionCount(0);
            bufferedAccount.setComplete(false);

            bufferedAccounts.add(bufferedAccount);
        }

        abnAmroBufferedAccountRepository.save(bufferedAccounts);
    }

    private Optional<Credentials> getAbnAmroCredentials(User user) {
        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId()).stream()
                .filter(c -> PROVIDERS_TO_RESYNC.contains(c.getProviderName()) && !AbnAmroUtils.Predicates.IS_BLOCKED
                        .apply(c)).collect(Collectors.toList());

        Preconditions.checkState(credentials.size() <= 1, "More than one ABN AMRO Credentials is not expected");

        return credentials.stream().findFirst();
    }

    private void cleanDuplicates(User user, Credentials credentials, CleanDuplicatesConfig cleanDuplicatesConfig) {

        if (waitUntilCredentialsIsUpdated(credentials, cleanDuplicatesConfig.getSleepSeconds())) {
            CleanupDuplicateTransactionsCommand command = new CleanupDuplicateTransactionsCommand();

            command.cleanup(serviceContext, cleanDuplicatesConfig.getForceDeleteOldest(),
                    cleanDuplicatesConfig.getOnlyCompareExternalId(), cleanDuplicatesConfig.isDryRun(), user,
                    new AtomicInteger());
        } else {
            log.error(user.getId(), "Credentials not in status 'UPDATED'. Not cleaning duplicates.");
        }
    }

    private boolean waitUntilCredentialsIsUpdated(Credentials credentials, int waitSeconds) {

        for (int i = 0; i < CREDENTIALS_UPDATED_RETRIES; i++) {

            Uninterruptibles.sleepUninterruptibly(waitSeconds, TimeUnit.SECONDS);

            if (isCredentialsUpdated(credentials)) {
                return true;
            }

            log.info(credentials, "Credentials not updated. Waiting...");
        }

        return false;
    }

    private boolean isCredentialsUpdated(Credentials credentials) {

        Credentials updatedCredentials = credentialsRepository.findOne(credentials.getId());

        return updatedCredentials != null && updatedCredentials.getStatus() == CredentialsStatus.UPDATED;
    }

    private void updateCredentialsStatus(Credentials credentials, CredentialsStatus status) {

        credentials.setStatus(status);

        UpdateCredentialsStatusRequest updateCredentialsRequest = new UpdateCredentialsStatusRequest();

        updateCredentialsRequest.setCredentials(credentials);
        updateCredentialsRequest.setUpdateContextTimestamp(true);
        updateCredentialsRequest.setUserId(credentials.getUserId());

        serviceContext.getSystemServiceFactory().getUpdateService().updateCredentials(updateCredentialsRequest);
    }

    private void countNumberOfTransactions(User user) {

        List<Transaction> transactions = transactionDao.findAllByUserId(user.getId());
        List<Account> accounts = accountRepository.findByUserId(user.getId());

        ImmutableMap<String, Account> accountsById = FluentIterable.from(accounts).uniqueIndex(Account::getId);

        ImmutableListMultimap<String, Transaction> transactionsByAccountId = FluentIterable.from(transactions)
                .index(Transaction::getAccountId);

        for (String accountId : accountsById.keySet()) {

            Account account = accountsById.get(accountId);

            ImmutableList<Transaction> transactionsByAccount = transactionsByAccountId.get(accountId);

            int transactionCount = transactionsByAccount == null ? 0 : transactionsByAccount.size();

            boolean isRejected = AbnAmroUtils.isAccountRejected(account);

            log.info(user.getId(),
                    String.format("Account: %s Rejected: %s Transactions: %d", account.getAccountNumber(), isRejected,
                            transactionCount));

        }

        log.info(String.format("Total number of transactions: %d", transactions.size()));
    }
}

class CleanDuplicatesConfig {

    private boolean cleanDuplicates;
    private boolean dryRun;
    private boolean forceDeleteOldest;
    private boolean onlyCompareExternalId;
    private int sleepSeconds;

    public boolean shouldCleanDuplicates() {
        return cleanDuplicates;
    }

    public void setCleanDuplicates(boolean cleanDuplicates) {
        this.cleanDuplicates = cleanDuplicates;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean getForceDeleteOldest() {
        return forceDeleteOldest;
    }

    public void setForceDeleteOldest(boolean forceDeleteOldest) {
        this.forceDeleteOldest = forceDeleteOldest;
    }

    public void setSleepSeconds(int sleepSeconds) {
        this.sleepSeconds = sleepSeconds;
    }

    public int getSleepSeconds() {
        return sleepSeconds;
    }

    public boolean getOnlyCompareExternalId() {
        return onlyCompareExternalId;
    }

    public void setOnlyCompareExternalId(boolean onlyCompareExternalId) {
        this.onlyCompareExternalId = onlyCompareExternalId;
    }
}
