package se.tink.backend.connector.cli;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Injector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.dropwizard.setup.Bootstrap;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.connector.client.ClientUtils;
import se.tink.backend.connector.rpc.AccountEntity;
import se.tink.backend.connector.rpc.AccountListEntity;
import se.tink.backend.connector.rpc.CreateTransactionAccountContainer;
import se.tink.backend.connector.rpc.CreateTransactionAccountEntity;
import se.tink.backend.connector.rpc.CreateTransactionEntity;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.connector.rpc.TransactionEntity;
import se.tink.backend.connector.rpc.UserEntity;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.libraries.uuid.UUIDUtils;

public class ConnectorBenchmarkCommand extends ConnectorCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(ConnectorBenchmarkCommand.class);

    private static final Date TODAY = new Date();
    private static final int LOAD_TEST_NUMBER_OF_ACCOUNTS_PER_USER = Integer.getInteger("numberOfAccountsPerUser", 2);
    private static final int LOAD_TEST_NUMBER_OF_USERS = Integer.getInteger("numberOfUsers", 500);
    private static final int LOAD_TEST_FIRST_USER_NUMBER_OFFSET = Integer.getInteger("firstUserNumberOffset", 0);
    private static final int LOAD_TEST_THREAD_POOL_SIZE = Integer.getInteger("threadPoolSize", 20);
    private static final long LOAD_TEST_TERMINATION_TIMEOUT_MIN = Integer.getInteger("threadPoolShutdownTimeoutMin", 5);
    private static final int LOAD_TEST_SINGLE_TRANSACTIONS_COUNT = Integer.getInteger("singleTransactions", 100000);
    private static final int LOAD_TEST_HISTORICAL_TRANSACTIONS_COUNT = Integer.getInteger("historicalTransactions",
            600);
    private static final int SINGLE_TRANSACTIONS_PER_SECOND = Integer.getInteger("singleTransactionsPerSecond", 1000);

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:9098/connector");
    private static final String CREATE_USER_URL = BASE_URL + "/users";
    private static final String DELETE_USER_URL = BASE_URL + "/users/%s";
    private static final String CREATE_ACCOUNTS_URL = BASE_URL + "/users/%s/accounts";
    private static final String CREATE_TRANSACTIONS_URL = BASE_URL + "/users/%s/transactions";
    private static String token;

    private static final AtomicInteger loadTestRemainingSingleTransactionsCount = new AtomicInteger();
    private static final Random RANDOM = new Random(42); // Fixating seed to really have reproducible benchmarks.
    private Client client;

    private enum Task {
        CREATE_USERS_AND_ACCOUNTS,
        HISTORICAL_TRANSACTIONS,
        SINGLE_TRANSACTIONS,
        DELETE_USERS,
        CREATE_USERS_AND_ACCOUNTS_AND_TRANSACTIONS,
    }

    private List<CreateTransactionEntity> transactions;
    private List<String> accountIds;
    private ExecutorService executor;
    private List<String> userIds;

    public ConnectorBenchmarkCommand() {
        super("benchmark-connector", "Benchmark general connector.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector) throws Exception {

        token = configuration.getConnector().getClients().get("CONNECTOR_CLIENT").get(0);
        client = ClientUtils.createBasicClient();

        log.debug("setup");

        int firstUserId = LOAD_TEST_FIRST_USER_NUMBER_OFFSET + 1;
        int lastUserId = LOAD_TEST_FIRST_USER_NUMBER_OFFSET + LOAD_TEST_NUMBER_OF_USERS;

        userIds = ContiguousSet.create(Range.closed(firstUserId, lastUserId), DiscreteDomain.integers()).stream()
                .map(integer -> String.format("user-%s", integer))
                .collect(Collectors.toList());

        accountIds = ContiguousSet
                .create(Range.closed(1, LOAD_TEST_NUMBER_OF_ACCOUNTS_PER_USER), DiscreteDomain.integers()).stream()
                .map(integer -> String.format("account-%s", integer)).collect(Collectors.toList());

        transactions = getTestTransactions();

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

    private List<CreateTransactionEntity> getTestTransactions() {
        List<CreateTransactionEntity> transactions = Lists.newArrayList();
        long start = LocalDateTime.of(2016, 1, 1, 0, 0, 0).atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;
        long end = LocalDateTime.of(2017, 10, 1, 0, 0, 0).atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;

        for (int i = 0; i < LOAD_TEST_HISTORICAL_TRANSACTIONS_COUNT; i++) {
            CreateTransactionEntity transactionEntity = new CreateTransactionEntity();
            transactionEntity.setAmount(ThreadLocalRandom.current().nextDouble(-5000, 5000));
            transactionEntity.setDescription(UUID.randomUUID().toString());
            transactionEntity.setType(i % 2 == 0 ? TransactionTypes.CREDIT_CARD : TransactionTypes.PAYMENT);
            transactionEntity.setExternalId(UUID.randomUUID().toString());
            transactionEntity.setDate(new Date(ThreadLocalRandom.current().nextLong(start, end)));

            transactions.add(transactionEntity);
        }

        return transactions;
    }

    private void loadHistoricalTransactions() throws InterruptedException {
        for (final String userId : userIds) {
            executor.execute(() -> {
                try {
                    CreateTransactionAccountContainer historicalContainer = createHistoricalContainer();

                    createClientRequest(String.format(CREATE_TRANSACTIONS_URL, userId)).post(historicalContainer);

                    int count = 0;
                    for (CreateTransactionAccountEntity transactionAccountEntity : historicalContainer
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

        loadTestRemainingSingleTransactionsCount.set(LOAD_TEST_SINGLE_TRANSACTIONS_COUNT);

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
                CreateTransactionAccountContainer container = createRealTimeContainer(accountId);

                Stopwatch timerSend = Stopwatch.createStarted();
                try {
                    createClientRequest(String.format(CREATE_TRANSACTIONS_URL, userId)).post(container);
                } catch (Exception e) {
                    log.error(String.format("Unable to send single transaction for account %s", accountId), e);
                }

                timerSend.stop();

                timerTotal.stop();

                TransactionEntity transaction = container.getTransactionAccounts().get(0).getTransactions().get(0);

                long elapsedTimeSend = timerSend.elapsed(TimeUnit.MILLISECONDS);
                long elapsedTimeTotal = timerTotal.elapsed(TimeUnit.MILLISECONDS);
                log.debug(String.format("[account:%s, remaining:%s] took %sms (%sms). %s", accountId,
                        loadTestRemainingSingleTransactionsCount.decrementAndGet(), elapsedTimeTotal,
                        elapsedTimeSend, transaction.toString()));
            } catch (Exception e) {
                log.error("Something went wrong.", e);
            }
        };
    }

    private void deleteUsers() throws InterruptedException {
        for (final String userId : userIds) {
            executor.execute(() -> {
                try {
                    createClientRequest(String.format(DELETE_USER_URL, userId)).delete();
                } catch (Exception e) {
                    log.error("Could not delete user: " + userId, e);
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
                    createClientRequest(CREATE_USER_URL).post(createUser(userId));
                    createClientRequest(String.format(CREATE_ACCOUNTS_URL, userId)).post(createAccounts());
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
                    createClientRequest(CREATE_USER_URL).post(createUser(userId));
                    createClientRequest(String.format(CREATE_ACCOUNTS_URL, userId)).post(createAccounts());
                    createClientRequest(String.format(CREATE_TRANSACTIONS_URL, userId))
                            .post(createHistoricalContainer());
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
        user.setToken(UUIDUtils.generateUUID());

        return user;
    }

    private CreateTransactionAccountContainer createRealTimeContainer(String accountId) {
        List<CreateTransactionAccountEntity> transactionAccounts = Lists.newArrayList(createTransactionAccount(
                accountId, Lists.newArrayList(createRealTimeTransaction())));

        CreateTransactionAccountContainer container = new CreateTransactionAccountContainer();
        container.setTransactionAccounts(transactionAccounts);
        container.setType(TransactionContainerType.REAL_TIME);

        return container;
    }

    private CreateTransactionEntity createRealTimeTransaction() {
        CreateTransactionEntity transaction = transactions.get(RANDOM.nextInt(transactions.size()));

        transaction.setDate(TODAY);
        transaction.setExternalId(UUID.randomUUID().toString());

        return transaction;
    }

    private CreateTransactionAccountContainer createHistoricalContainer() {
        List<CreateTransactionAccountEntity> transactionAccounts = accountIds.stream()
                .map(accountId -> createTransactionAccount(accountId, transactions))
                .collect(Collectors.toList());

        CreateTransactionAccountContainer container = new CreateTransactionAccountContainer();
        container.setTransactionAccounts(transactionAccounts);
        container.setType(TransactionContainerType.HISTORICAL);

        return container;
    }

    private CreateTransactionAccountEntity createTransactionAccount(String accountId,
            List<CreateTransactionEntity> transactions) {
        CreateTransactionAccountEntity transactionAccount = new CreateTransactionAccountEntity();

        transactionAccount.setExternalId(accountId);
        transactionAccount.setBalance(1000d);
        transactionAccount.setReservedAmount(10d);
        transactionAccount.setTransactions(transactions);

        return transactionAccount;
    }

    private AccountListEntity createAccounts() {
        List<AccountEntity> accounts = Lists.newArrayList();

        for (String accountId : accountIds) {
            AccountEntity account = new AccountEntity();
            account.setExternalId(accountId);
            account.setName(accountId);
            account.setBalance(400d);
            account.setReservedAmount(account.getBalance());
            account.setNumber(accountId);
            account.setType(AccountTypes.CHECKING);
            accounts.add(account);
        }

        AccountListEntity listEntity = new AccountListEntity();
        listEntity.setAccounts(accounts);
        return listEntity;
    }

    private WebResource.Builder createClientRequest(String url) {
        return client.resource(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header("Authorization", "token " + token);
    }
}
