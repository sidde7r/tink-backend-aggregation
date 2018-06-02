package se.tink.backend.connector.cli;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.utils.DemoDataUtils;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.connector.api.SEBConnectorService;
import se.tink.backend.connector.configuration.seb.SebCommandModule;
import se.tink.backend.connector.resources.SEBConnectorServiceResource;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.connector.rpc.seb.AccountEntity;
import se.tink.backend.connector.rpc.seb.AccountListEntity;
import se.tink.backend.connector.rpc.seb.PartnerTransactionPayload;
import se.tink.backend.connector.rpc.seb.TransactionAccountContainer;
import se.tink.backend.connector.rpc.seb.TransactionAccountEntity;
import se.tink.backend.connector.rpc.seb.TransactionEntity;
import se.tink.backend.connector.rpc.seb.UserEntity;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;

public class SEBConnectorBenchmarkCommand extends ConnectorCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(SEBConnectorBenchmarkCommand.class);

    private static final Date TODAY = new Date();
    private static final int LOAD_TEST_NUMBER_OF_ACCOUNTS_PER_USER = Integer.getInteger("numberOfAccountsPerUser", 4);
    private static final int LOAD_TEST_NUMBER_OF_USERS = Integer.getInteger("numberOfUsers", 500);
    private static final int LOAD_TEST_FIRST_USER_NUMBER_OFFSET = Integer.getInteger("firstUserNumberOffset", 0);
    private static final int LOAD_TEST_THREAD_POOL_SIZE = Integer.getInteger("threadPoolSize", 20);
    private static final long LOAD_TEST_TERMINATION_TIMEOUT_MIN = Integer.getInteger("threadPoolShutdownTimeoutMin", 5);
    private static final int LOAD_TEST_SINGLE_TRANSACTIONS_COUNT = Integer.getInteger("singleTransactions", 100000);
    private static final int LOAD_TEST_HISTORICAL_TRANSACTIONS_COUNT = Integer.getInteger("historicalTransactions",
            1200);
    private static final int SINGLE_TRANSACTIONS_PER_SECOND = Integer.getInteger("singleTransactionsPerSecond", 1000);

    private static final AtomicInteger LOAD_TEST_REMAINING_SINGLE_TRANSACTIONS_COUNT = new AtomicInteger();
    private static final Random RANDOM = new Random(42); // Fixating seed to really have reproducible benchmarks.
    private SEBConnectorService sebConnectorService;

    private enum Task {
        CREATE_USERS_AND_ACCOUNTS,
        HISTORICAL_TRANSACTIONS,
        SINGLE_TRANSACTIONS,
        DELETE_USERS,
        CREATE_USERS_AND_ACCOUNTS_AND_TRANSACTIONS,
    }

    private List<TransactionEntity> transactions;
    private List<String> accountIds;
    private ExecutorService executor;
    private List<String> userIds;

    public SEBConnectorBenchmarkCommand() {
        super("benchmark-seb-connector", "Benchmark SEB connector.");
    }

    private static List<TransactionEntity> loadTransactions() throws IOException {
        return DemoDataUtils.readTransactions(
                new File("data/demo/201212121212/9999-111111111111.txt"),
                new Account(),
                true).stream().map(t -> {
            TransactionEntity te = new TransactionEntity();
            te.setAmount(t.getAmount());
            te.setDate(t.getDate());
            te.setDescription(t.getDescription());
            te.setType(t.getType());
            te.setExternalId(UUID.randomUUID().toString());
            return te;
        }).limit(LOAD_TEST_HISTORICAL_TRANSACTIONS_COUNT).collect(Collectors.toList());
    }

    private void loadHistoricalTransactions() throws InterruptedException {
        for (final String userId : userIds) {
            executor.execute(() -> {
                try {
                    TransactionAccountContainer historicalContainer = createHistoricalContainer();

                    sebConnectorService.transactions(userId, historicalContainer);

                    int count = 0;
                    for (TransactionAccountEntity transactionAccountEntity : historicalContainer
                            .getTransactionAccounts()) {
                        count += transactionAccountEntity.getTransactions().size();
                    }

                    log.info(String.format("Sent %d transactions.", count));
                } catch (Exception e) {
                    log.error("Could not load historical transactions: " + userId, e);
                }
            });
        }

        executor.shutdown();
        if (!executor.awaitTermination(LOAD_TEST_TERMINATION_TIMEOUT_MIN, TimeUnit.MINUTES)) {
            executor.shutdownNow();
        }
    }

    private void realTimeTransactionsLoad() throws Exception {
        log.debug("realTimeTransactionsLoad");

        RateLimiter rateLimiter = RateLimiter.create(SINGLE_TRANSACTIONS_PER_SECOND);

        LOAD_TEST_REMAINING_SINGLE_TRANSACTIONS_COUNT.set(LOAD_TEST_SINGLE_TRANSACTIONS_COUNT);

        for (int remaining = LOAD_TEST_SINGLE_TRANSACTIONS_COUNT; remaining > 0; remaining--) {
            // Pick random user id.
            String userId = userIds.get(RANDOM.nextInt(userIds.size()));
            // Pick random account id.
            String accountId = accountIds.get(RANDOM.nextInt(accountIds.size()));

            rateLimiter.acquire();
            executor.execute(getSingleTransactionSender(userId, accountId));
        }

        executor.shutdown();
        if (!executor.awaitTermination(LOAD_TEST_TERMINATION_TIMEOUT_MIN, TimeUnit.MINUTES)) {
            executor.shutdownNow();
        }
    }

    private Runnable getSingleTransactionSender(final String userId, final String accountId) {
        return () -> {
            try {
                Stopwatch timerTotal = Stopwatch.createStarted();
                TransactionAccountContainer container = createRealTimeContainer(accountId);

                Stopwatch timerSend = Stopwatch.createStarted();
                try {
                    sebConnectorService.transactions(userId, container);
                } catch (Exception e) {
                    log.error(String.format("Unable to send single transaction for account %s", accountId), e);
                }
                timerSend.stop();

                timerTotal.stop();

                TransactionEntity transaction = container.getTransactionAccounts().get(0).getTransactions().get(0);

                long elapsedTimeSend = timerSend.elapsed(TimeUnit.MILLISECONDS);
                long elapsedTimeTotal = timerTotal.elapsed(TimeUnit.MILLISECONDS);
                log.debug(String.format("[account:%s, remaining:%s] took %sms (%sms). %s", accountId,
                        LOAD_TEST_REMAINING_SINGLE_TRANSACTIONS_COUNT.decrementAndGet(), elapsedTimeTotal,
                        elapsedTimeSend, transaction.toString()));
            } catch (Exception e) {
                log.error("Something went wrong.", e);
            }
        };
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector) throws Exception {

        injector = injector.createChildInjector(Lists.newArrayList(new SebCommandModule()));
        sebConnectorService = injector.getInstance(SEBConnectorServiceResource.class);

        log.debug("setup");

        int firstUserId = LOAD_TEST_FIRST_USER_NUMBER_OFFSET + 1;
        int lastUserId = LOAD_TEST_FIRST_USER_NUMBER_OFFSET + LOAD_TEST_NUMBER_OF_USERS;

        userIds = ContiguousSet.create(Range.closed(firstUserId, lastUserId), DiscreteDomain.integers()).stream()
                .map(integer -> String.format("user-%s", integer))
                .collect(Collectors.toList());

        accountIds = ContiguousSet
                .create(Range.closed(1, LOAD_TEST_NUMBER_OF_ACCOUNTS_PER_USER), DiscreteDomain.integers()).stream()
                .map(integer -> String.format("account-%s", integer)).collect(Collectors.toList());

        transactions = loadTransactions();

        Task task = Task.valueOf(System.getProperty("task"));
        executor = Executors.newFixedThreadPool(LOAD_TEST_THREAD_POOL_SIZE);

        switch (task) {
        case CREATE_USERS_AND_ACCOUNTS:
            createUsersAndAccounts();
            break;
        case HISTORICAL_TRANSACTIONS:
            loadHistoricalTransactions();
            break;
        case SINGLE_TRANSACTIONS:
            realTimeTransactionsLoad();
            break;
        case DELETE_USERS:
            deleteUsers();
            break;
        case CREATE_USERS_AND_ACCOUNTS_AND_TRANSACTIONS:
            createUsersAndAccountsAndTransactions();
            break;
        }

    }

    private void deleteUsers() throws InterruptedException {
        for (final String userId : userIds) {
            executor.execute(() -> {
                try {
                    sebConnectorService.deleteUser(userId);
                } catch (Exception e) {
                    log.error("Could not create user: " + userId, e);
                }
            });
        }

        executor.shutdown();
        if (!executor.awaitTermination(LOAD_TEST_TERMINATION_TIMEOUT_MIN, TimeUnit.MINUTES)) {
            executor.shutdownNow();
        }
    }

    private void createUsersAndAccounts() throws InterruptedException {
        for (final String userId : userIds) {
            executor.execute(() -> {
                try {
                    sebConnectorService.user(createUser(userId));
                    sebConnectorService.accounts(userId, createAccounts());
                } catch (Exception e) {
                    log.error("Could not create user: " + userId, e);
                }
            });
        }

        executor.shutdown();
        if (!executor.awaitTermination(LOAD_TEST_TERMINATION_TIMEOUT_MIN, TimeUnit.MINUTES)) {
            executor.shutdownNow();
        }
    }

    private void createUsersAndAccountsAndTransactions() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        int count = 0;

        for (final String userId : userIds) {
            count++;
            final int finalCount = count;

            executor.execute(() -> {
                try {
                    sebConnectorService.user(createUser(userId));
                    sebConnectorService.accounts(userId, createAccounts());
                    sebConnectorService.transactions(userId, createHistoricalContainer());

                    log.info(String.format("Created user, accounts and transactions for %s users.", finalCount));
                } catch (Exception e) {
                    log.error("Could not enroll user: " + userId, e);
                }
            });
        }

        executor.shutdown();
        if (!executor.awaitTermination(LOAD_TEST_TERMINATION_TIMEOUT_MIN, TimeUnit.MINUTES)) {
            executor.shutdownNow();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info(String.format("RESULTS: Enrolled %s users in %s seconds.", count, duration / 1000));
    }

    private UserEntity createUser(String userId) {
        UserEntity user = new UserEntity();
        user.setExternalId(userId);

        return user;
    }

    private TransactionAccountContainer createRealTimeContainer(String accountId) {
        List<TransactionAccountEntity> transactionAccounts = Lists.newArrayList(createTransactionAccount(
                accountId, Lists.newArrayList(createRealTimeTransaction())));

        String checkpointId = System.getProperty("checkpointId");
        if (checkpointId != null) {
            for (TransactionAccountEntity transactionAccount : transactionAccounts) {
                for (TransactionEntity transaction : transactionAccount.getTransactions()) {
                    transaction.getPayload().put(PartnerTransactionPayload.CHECKPOINT_ID, checkpointId);
                }
            }

        }

        TransactionAccountContainer container = new TransactionAccountContainer();
        container.setTransactionAccounts(transactionAccounts);
        container.setType(TransactionContainerType.REAL_TIME);

        return container;
    }

    private TransactionEntity createRealTimeTransaction() {
        TransactionEntity transaction = transactions.get(RANDOM.nextInt(transactions.size()));

        transaction.setDate(TODAY);
        transaction.setExternalId(UUID.randomUUID().toString());

        return transaction;
    }

    private TransactionAccountContainer createHistoricalContainer() {
        List<TransactionAccountEntity> transactionAccounts = accountIds.stream()
                .map(accountId -> createTransactionAccount(accountId, transactions))
                .collect(Collectors.toList());

        TransactionAccountContainer container = new TransactionAccountContainer();
        container.setTransactionAccounts(transactionAccounts);
        container.setType(TransactionContainerType.HISTORICAL);

        return container;
    }

    private TransactionAccountEntity createTransactionAccount(String accountId,
            List<TransactionEntity> transactions) {
        TransactionAccountEntity transactionAccount = new TransactionAccountEntity();

        transactionAccount.setExternalId(accountId);
        transactionAccount.setBalance(1000d);
        transactionAccount.setDisposableAmount(990d);
        transactionAccount.setTransactions(transactions);

        return transactionAccount;
    }

    private AccountListEntity createAccounts() {
        List<AccountEntity> as = Lists.newArrayList();

        for (String accountId : accountIds) {
            AccountEntity a = new AccountEntity();
            a.setExternalId(accountId);
            a.setName(accountId);
            a.setBalance(400d);
            a.setDisposableAmount(a.getBalance());
            a.setNumber(accountId);
            a.setType(AccountTypes.CHECKING);
            as.add(a);
        }

        AccountListEntity accounts = new AccountListEntity();
        accounts.setAccounts(as);
        return accounts;
    }
}
