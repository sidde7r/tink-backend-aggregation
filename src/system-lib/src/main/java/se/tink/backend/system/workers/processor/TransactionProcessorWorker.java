package se.tink.backend.system.workers.processor;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.lifecycle.Managed;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.zookeeper.KeeperException;
import org.joda.time.DateTime;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.ElementMonitoredQueue;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.ListenableThreadPoolSubmitter;
import se.tink.backend.common.concurrency.NamedCallable;
import se.tink.backend.common.concurrency.NamedRunnable;
import se.tink.backend.common.concurrency.PrioritizedCallable;
import se.tink.backend.common.concurrency.PrioritizedRunnable;
import se.tink.backend.common.concurrency.PriorityExecutorQueueFactory;
import se.tink.backend.common.concurrency.TransactionInMemoryReadWriteLock;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedCallableListenableFutureTask;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.mapper.CoreTransactionMapper;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Loan;
import se.tink.backend.core.StatisticGenerationMode;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserState;
import se.tink.backend.core.property.Property;
import se.tink.backend.system.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.system.rpc.ProcessTransactionsRequest;
import se.tink.backend.system.statistics.TransactionInstrumentationReporter;
import se.tink.backend.system.workers.processor.chaining.ChainFactory;
import se.tink.backend.system.workers.processor.chaining.UserChainFactoryCreator;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.jersey.logging.UserRuntimeException;
import se.tink.libraries.metrics.ListSizeBuckettedTimer;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;

public class TransactionProcessorWorker implements Managed {
    private static final LogUtils log = new LogUtils(TransactionProcessorWorker.class);
    private static final Predicate<PrioritizedRunnable> IS_HIGH_PRIORITY_RUNNABLE = input ->
            input.priority <= PrioritizedRunnable.HIGH_PRIORITY;

    private static final Function<WrappedRunnableListenableFutureTask<PrioritizedRunnable, ?>, String>
            PRIORITY_RUNNABLE_KEY_EXTRACTOR = Functions
            .compose(new ElementMonitoredQueue.PrioritizedRunnableLabelExtractor(),
                    new WrappedRunnableListenableFutureTask.DelegateExtractor<>());

    private static final long PROCESSING_LOCK_TIMEOUT_MINUTES = 5;

    // Thread pool resource parameters.
    private static final int MAX_QUEUED_CPU_REQUESTS = 200;
    private static final int NBR_OF_CPU_THREADS = 10; // When we checked beginning of 2016, one UserData instance took up ~10 MB.

    private static final String TRANSACTION_PROCESSOR_CPU_THREAD_PREFIX = "transaction-processor-cpu-thread-";
    private static final String TRANSACTION_PROCESSOR_CPU_THREAD_BY_NUMBER =
            TRANSACTION_PROCESSOR_CPU_THREAD_PREFIX + "-%d";
    private static final String TRANSACTION_PROCESSOR_CPU_THREAD_BY_STRING =
            TRANSACTION_PROCESSOR_CPU_THREAD_PREFIX + "-%s";

    private static final ThreadFactory TRANSACTIONS_CPU_THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat(TRANSACTION_PROCESSOR_CPU_THREAD_BY_NUMBER).build();

    private static final ImmutableList<Integer> TRANSACTIONS_LIST_SIZE_BUCKETS = ImmutableList
            .of(0, 50, 100, 250, 500, 1000, 1500, 2000, 3000, 5000, 10000, 20000, 30000,
                    40000);
    private static final ImmutableList<Integer> ACCOUNT_BALANCES_LIST_SIZE_BUCKETS = ImmutableList.of(
            // TODO: Verify if this makes sense once this is out in production. I have no idea about
            // magnitude here.
            0,
            50,
            100,
            250,
            500,
            1000,
            1500,
            2000,
            3000,
            5000,
            10000,
            20000,
            40000,
            100000);
    private static final ImmutableList<Integer> ACCOUNTS_LIST_SIZE_BUCKETS = ImmutableList
            .of(0, 1, 2, 3, 4, 6, 8, 12, 16, 20, 30, 40, 50, 60);
    private static final ImmutableList<Integer> CREDENTIALS_LIST_SIZE_BUCKETS = ImmutableList
            .of(0, 1, 2, 3, 4, 5, 6, 9, 15, 30);
    private static final ImmutableList<Integer> LOANS_LIST_SIZE_BUCKETS = ImmutableList
            .of(0, 1, 2, 3, 4, 5, 6, 9, 15, 30);
    private static final ImmutableList<Integer> PROPERTIES_LIST_SIZE_BUCKETS = ImmutableList.of(0, 1, 2, 3, 4);

    private static final String TRANSACTION_PROCESSOR_IO_THREAD_PREFIX = "transaction-processor-io-thread";
    private static final String TRANSACTION_PROCESSOR_IO_THREAD_BY_NUMBER =
            TRANSACTION_PROCESSOR_IO_THREAD_PREFIX + "-%d";
    private static final String TRANSACTION_PROCESSOR_IO_THREAD_BY_STRING =
            TRANSACTION_PROCESSOR_IO_THREAD_PREFIX + "-%s";

    private static final ThreadFactory IO_THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat(TRANSACTION_PROCESSOR_IO_THREAD_BY_NUMBER).build();

    private final UserChainFactoryCreator chainFactoryCreator;
    private final MetricRegistry metricRegistry;

    private ListenableThreadPoolExecutor<PrioritizedRunnable> cpuThreadPool;
    private ListenableThreadPoolSubmitter<PrioritizedCallable<?>> ioThreadPool;
    private static final ImmutableList<Double> LONGER_DURATION_BUCKETS = ImmutableList.of(
            0., .005, .01, .025, .05, .1, .25, .5, 1., 2.5, 5., 10., 15., 20., 25., 30.
    );

    private TransactionProcessor transactionProcessor;
    private TransactionInstrumentationReporter transactionInstrumentationReporter;
    private UserRepository userRepository;
    private final ServiceContext serviceContext;
    private CredentialsRepository credentialsRepository;
    private AccountRepository accountRepository;
    private AccountBalanceHistoryRepository accountBalanceHistoryRepository;
    private LoanDataRepository loanDataRepository;
    private TransactionDao transactionDao;
    private UserStateRepository userStateRepository;
    private CuratorFramework coordinationClient;
    private PropertyRepository propertyRepository;

    private static final MetricId INSTRUMENTATION = MetricId.newId("process_instrumention");
    private Timer instrumentationReporterTimer;

    private static final MetricId TOTAL_PROCESS_TRANSACTIONS_DURATION_METRIC_NAME = MetricId.newId
            ("process_transactions_duration");
    private Timer totalDurationTimer;

    private static final MetricId PROCESS_TRANSACTIONS_SUBSTEP_DURATION_METRIC_NAME = MetricId.newId
            ("process_transactions_substep_duration");
    private Timer acquireLocksTimer;
    private Timer constructCommandsTimer;
    private Timer loadUserDataTimer;
    private Timer buildContextTimer;
    private Timer processTimer;
    private Timer releaseLocksTimer;
    private Timer generateStatisticsAndActivitiesTimer;
    private Timer releaseTransactionInMemoryLockTimer;

    private static final MetricId PROCESS_TRANSACTIONS_POPULATE_USER_DATA = MetricId.newId
            ("process_transactions_populate_userdata");
    private ListSizeBuckettedTimer loadTransactionsTimer;
    private ListSizeBuckettedTimer loadAccountsTimer;
    private ListSizeBuckettedTimer loadAccountBalancesTimer;
    private ListSizeBuckettedTimer loadCredentialsTimer;
    private Timer loadUserStateTimer;
    private ListSizeBuckettedTimer loadPropertiesTimer;
    private ListSizeBuckettedTimer loanDataTimer;

    private static final Date EARLIEST_DATE_ALLOWED = new Date(0);
    private static final ZoneId CET = ZoneId.of("CET");
    public TransactionProcessorWorker(
            final ServiceContext serviceContext,
            UserChainFactoryCreator chainFactoryCreator, MetricRegistry metricRegistry
    ) {

        this.serviceContext = serviceContext;
        this.chainFactoryCreator = chainFactoryCreator;
        this.metricRegistry = metricRegistry;

        initializeRepositories(serviceContext);
        initializeMetrics();

        this.transactionProcessor = new TransactionProcessor(
                metricRegistry
        );
    }

    private void initializeRepositories(final ServiceContext serviceContext) {
        userRepository = serviceContext.getRepository(UserRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        accountBalanceHistoryRepository = serviceContext.getRepository(AccountBalanceHistoryRepository.class);
        loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        userStateRepository = serviceContext.getRepository(UserStateRepository.class);
        propertyRepository = serviceContext.getRepository(PropertyRepository.class);
        transactionInstrumentationReporter = new TransactionInstrumentationReporter(serviceContext.getConfiguration(),
                metricRegistry);
        coordinationClient = serviceContext.getCoordinationClient();
    }

    private void initializeMetrics() {
        instrumentationReporterTimer = metricRegistry.timer(INSTRUMENTATION);
        totalDurationTimer = metricRegistry
                .timer(TOTAL_PROCESS_TRANSACTIONS_DURATION_METRIC_NAME, LONGER_DURATION_BUCKETS);
        acquireLocksTimer = metricRegistry
                .timer(PROCESS_TRANSACTIONS_SUBSTEP_DURATION_METRIC_NAME.label("step", "acquire_lock"));
        constructCommandsTimer = metricRegistry
                .timer(PROCESS_TRANSACTIONS_SUBSTEP_DURATION_METRIC_NAME.label("step", "construct_commands"));
        loadUserDataTimer = metricRegistry
                .timer(PROCESS_TRANSACTIONS_SUBSTEP_DURATION_METRIC_NAME.label("step", "load_user_data"));
        buildContextTimer = metricRegistry
                .timer(PROCESS_TRANSACTIONS_SUBSTEP_DURATION_METRIC_NAME.label("step", "build_context"));
        processTimer = metricRegistry
                .timer(PROCESS_TRANSACTIONS_SUBSTEP_DURATION_METRIC_NAME.label("step", "process"));
        releaseLocksTimer = metricRegistry
                .timer(PROCESS_TRANSACTIONS_SUBSTEP_DURATION_METRIC_NAME.label("step", "release_lock"));
        generateStatisticsAndActivitiesTimer = metricRegistry
                .timer(PROCESS_TRANSACTIONS_SUBSTEP_DURATION_METRIC_NAME.label("step",
                        "generate_statistics_and_activities"));
        releaseTransactionInMemoryLockTimer = metricRegistry
                .timer(PROCESS_TRANSACTIONS_SUBSTEP_DURATION_METRIC_NAME.label("step",
                        "release_transaction_in_memory_lock"));

        // For the loaded userdata items that are lists, we use a ListSizedTimer.
        loadTransactionsTimer = new ListSizeBuckettedTimer(metricRegistry, PROCESS_TRANSACTIONS_POPULATE_USER_DATA
                .label("what", "transactions"), LONGER_DURATION_BUCKETS, TRANSACTIONS_LIST_SIZE_BUCKETS);
        loadAccountsTimer = new ListSizeBuckettedTimer(metricRegistry,
                PROCESS_TRANSACTIONS_POPULATE_USER_DATA.label("what", "accounts"),
                LONGER_DURATION_BUCKETS, ACCOUNTS_LIST_SIZE_BUCKETS);
        loadAccountBalancesTimer = new ListSizeBuckettedTimer(metricRegistry, PROCESS_TRANSACTIONS_POPULATE_USER_DATA
                .label("what", "account_balances"), LONGER_DURATION_BUCKETS,
                ACCOUNT_BALANCES_LIST_SIZE_BUCKETS);
        loadCredentialsTimer = new ListSizeBuckettedTimer(metricRegistry,
                PROCESS_TRANSACTIONS_POPULATE_USER_DATA.label("what", "credentials"),
                LONGER_DURATION_BUCKETS, CREDENTIALS_LIST_SIZE_BUCKETS);
        loadUserStateTimer = metricRegistry
                .timer(PROCESS_TRANSACTIONS_POPULATE_USER_DATA.label("what", "user_state"),
                        LONGER_DURATION_BUCKETS);
        loadPropertiesTimer = new ListSizeBuckettedTimer(metricRegistry,
                PROCESS_TRANSACTIONS_POPULATE_USER_DATA.label("what", "properties"), LONGER_DURATION_BUCKETS,
                PROPERTIES_LIST_SIZE_BUCKETS);
        loanDataTimer = new ListSizeBuckettedTimer(metricRegistry,
                PROCESS_TRANSACTIONS_POPULATE_USER_DATA.label("what", "loan_data"), LONGER_DURATION_BUCKETS,
                LOANS_LIST_SIZE_BUCKETS);
    }

    private static <V> void stopTimerOnSuccess(final ListenableFuture<V> future, final Context context) {
        Futures.addCallback(future, new FutureCallback<V>() {
            @Override
            public void onSuccess(@Nullable final V v) {
                context.stop();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                // Deliberately left empty.
            }
        });
    }

    public ListenableFuture<?> process(final ProcessTransactionsRequest processTransactionsRequest,
            final boolean rethrowAllExceptions) {
        final int priority = processTransactionsRequest.isUserTriggered() ? PrioritizedRunnable.HIGH_PRIORITY
                : PrioritizedRunnable.LOW_PRIORITY;

        final PrioritizedRunnable processRunnable = new PrioritizedRunnable(priority, new NamedRunnable(
                () -> {
                    try {
                        processInternal(priority, processTransactionsRequest, rethrowAllExceptions);
                    } catch (final Exception e) {
                        // Must rethrow exception for `ListenableFuture` to pick up an error.
                        throw new UserRuntimeException(processTransactionsRequest.getUserId(),
                                "Could not process transactions.", e);
                    }
                },
                String.format(TRANSACTION_PROCESSOR_CPU_THREAD_BY_STRING, processTransactionsRequest.getUserId())));

        final Context context = totalDurationTimer.time();
        final ListenableFuture<?> result = cpuThreadPool.execute(processRunnable);
        stopTimerOnSuccess(result, context);

        return result;
    }

    private void processInternal(final int priority, final ProcessTransactionsRequest processTransactionsRequest,
            final boolean rethrowAllExceptions) throws Exception {
        final String userId = processTransactionsRequest.getUserId();

        log.trace(userId, "Transaction processing started.");

        // Acquire locks.
        final Context acquireLocksTimerContext = acquireLocksTimer.time();

        final InterProcessMutex transactionInMemoryLock = new TransactionInMemoryReadWriteLock(coordinationClient,
                userId).getLockForHoldingTransactionsInMemory();
        List<Transaction> inBatchTransactions;
        try {

            if (!transactionInMemoryLock.acquire(15, TimeUnit.SECONDS)) {
                // This is not super serious as it might result in stale activities and/or statistics nothing else.
                // Therefor, we are simply logging a warning.
                log.warn(userId,
                        "Could not acquire in-memory lock when processing transactions. Continue processing...");
            } else {
                log.debug(userId, "Acquired transactions snapshot fetching lock");
            }

            // Load the raw transactions that have been injected.
            inBatchTransactions = CoreTransactionMapper.toCoreTransaction(processTransactionsRequest.getTransactions());

            LocalDate earliestDate = inBatchTransactions.stream().min(Comparator.comparing(Transaction::getOriginalDate)).get().getOriginalDate().toInstant().atZone(CET).toLocalDate();

            final InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(coordinationClient,
                    ProcessTransactionsRequest.LOCK_PREFIX_USER + userId);

            UserData userData;
            try {
                if (!lock.acquire(PROCESSING_LOCK_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                    // The lock couldn't be acquired within the specified time period, this is an error. It is however more
                    // important that we try to process the transactions anyway compared to throw them away. Retrying to
                    // acquire the lock will not succeed as long the process that holds the lock didn't released it.
                    log.error(userId, "Could not acquire lock when processing transactions. Continue processing...");
                } else {
                    log.debug(userId, "Acquired transaction processing lock");
                }

                acquireLocksTimerContext.stop();

                // Load user data.

                userData = loadUserData(priority, userId, earliestDate);
                if (userData.getUser() == null) {
                    log.warn(userId,
                            "Could not find the User. Probably due to user being deleted concurrently. Not continuing.");
                    return;
                }

                log.info(userId, processTransactionsRequest.getCredentialsId(), "Processing transactions...");

                final Context constructCommandsTimerContext = constructCommandsTimer.time();

                // Construct the command chain.

                ChainFactory commandChain = chainFactoryCreator.build();
                constructCommandsTimerContext.stop();

                // Create the context.

                final Context buildContextTimer = this.buildContextTimer.time();
                final TransactionProcessorContext context = new TransactionProcessorContext(
                        userData.getUser(),
                        serviceContext.getDao(ProviderDao.class).getProvidersByName(),
                        inBatchTransactions,
                        userData,
                        processTransactionsRequest.getCredentialsId()
                );
                buildContextTimer.stop();

                // Process the transactions.
                final Context processTimerContext = processTimer.time();
                transactionProcessor.processTransactions(context, commandChain, userData, rethrowAllExceptions);

                // Reset the transactions from user data since it may have changed, exclude transactions in future.

                final Date today = DateUtils.setInclusiveEndTime(new Date());

                userData.setTransactions(
                        Lists.newArrayList(context.getUserData().getInStoreTransactions()
                                .values().stream().filter(t -> {
                                    final Date transactionDate = t.getDate();
                                    return transactionDate.before(today) && EARLIEST_DATE_ALLOWED
                                            .before(transactionDate);
                                }).collect(Collectors.toList())));
                userData.setLoanDataByAccount(context.getUserData().getLoanDataByAccount());
                processTimerContext.stop();

            } catch (KeeperException.NoNodeException nne) {
                log.error("Couldn't acquire transaction processing lock ", nne);
                throw nne;
            } finally {
                // Release the write lock.

                final Context releaseLocksTimerContext = releaseLocksTimer.time();
                if (lock.isAcquiredInThisProcess()) {
                    log.debug(userId, "Will release transaction processing lock as the owner");

                    try {
                        lock.release();
                        log.debug(userId, "Released transaction processing lock");
                    } catch (KeeperException.NoNodeException nne) {
                        log.warn("Couldn't release transaction processing lock ", nne);
                    }
                } else {
                    log.warn(userId, "Write transaction processing lock is not acquired in this process");
                }
                releaseLocksTimerContext.stop();
            }

            if (serviceContext.getConfiguration().getTransactionProcessor().isStatisticsActivitiesEnabled()) {
                generateStatisticsAndActivities(processTransactionsRequest, userId, userData);
            } else {
                log.info("Skipping statistics and activities generation for user {}", userId);
            }

        } catch (KeeperException.NoNodeException nne) {
            log.error("Couldn't acquire transaction snapshot fetching lock ", nne);
            throw nne;
        } finally {

            final Context releaseLockTimerContext = releaseTransactionInMemoryLockTimer.time();
            if (transactionInMemoryLock.isAcquiredInThisProcess()) {
                log.debug(userId, "Will release transaction snapshot fetching lock as the owner");
                try {
                    transactionInMemoryLock.release();
                } catch (KeeperException.NoNodeException nne) {
                    log.warn("Couldn't release transaction snapshot fetching lock ", nne);
                }
                log.debug(userId, "Released transaction snapshot fetching lock");
            } else {
                log.debug(userId, "Transaction snapshot fetching lock is not owned");
            }
            releaseLockTimerContext.stop();
        }

        // Report metrics about the transactions

        final Context processStatisticsTimerContext = instrumentationReporterTimer.time();
        transactionInstrumentationReporter.report(inBatchTransactions, processTransactionsRequest.getTopic());
        processStatisticsTimerContext.stop();

        log.trace(userId, "Transaction processing finished.");
    }

    private void generateStatisticsAndActivities(ProcessTransactionsRequest processTransactionsRequest, String userId, UserData userData) {
        // There's an inherent race condition here. Two updateTransaction calls can lead to incorrect statistics.
        // This is because statistics is based on transactions from transaction chain, but there is no lock around
        // _both_ transaction chain and statistics calculation. If the second transaction process request starts its
        // statistics generation before the first transaction process, the statistics will be broken.
        //
        // Note, however, that the above scenario is very unlikely to happen. From [1]:
        //
        //     "[...] the probability that this happens is very unlikely as long as transaction chain takes much
        //     longer than then time between the Transaction-chain-lock is released and statistics/activity-lock is
        //     being acquired."
        //
        // [1] https://trello.com/c/eaxaLa8j/20-race-condition-could-create-incorrect-statistics-and-activities

        // Generate statistics.

        final Context generateStatisticsAndActivitiesTimerContext = generateStatisticsAndActivitiesTimer.time();
        final GenerateStatisticsAndActivitiesRequest generateStatisticsAndActivitiesRequest = new GenerateStatisticsAndActivitiesRequest();

        generateStatisticsAndActivitiesRequest.setMode(StatisticMode.FULL);
        generateStatisticsAndActivitiesRequest.setUserId(userId);
        generateStatisticsAndActivitiesRequest.setCredentialsId(processTransactionsRequest.getCredentialsId());
        generateStatisticsAndActivitiesRequest.setUserData(userData);
        generateStatisticsAndActivitiesRequest.setTakeReadlock(false);
        generateStatisticsAndActivitiesRequest.setUserTriggered(processTransactionsRequest.isUserTriggered());
        generateStatisticsAndActivitiesRequest.setStatisticGenerationMode(StatisticGenerationMode.APPEND);
        serviceContext.getSystemServiceFactory().getProcessService()
                .generateStatisticsAndActivitySynchronous(generateStatisticsAndActivitiesRequest);
        generateStatisticsAndActivitiesTimerContext.stop();
    }

    private UserData loadUserData(final int priority, final String userId, LocalDate earliestIncomingDate)
            throws ExecutionException, InterruptedException {

        final Context timerContext = loadUserDataTimer.time();

        User user = userRepository.findOne(userId);
        if (user == null) {
            return new UserData();
        }

        int monthsForProcessing = serviceContext.getConfiguration().getTransactionProcessor().getMonthsForProcessing();
        LocalDate defaultDateForProcessing = LocalDate.now().minusMonths(monthsForProcessing);

        final LocalDate processingLocalDate = earliestIncomingDate.isAfter(defaultDateForProcessing) ? defaultDateForProcessing : earliestIncomingDate;

        int periodAdjustedDay = user.getProfile().getPeriodMode().equals(ResolutionTypes.MONTHLY_ADJUSTED) ? user.getProfile().getPeriodAdjustedDay() : 1;
        Date processingDate = Date.from(processingLocalDate.atStartOfDay(CET).toInstant());
        // month ago on the period break day
        LocalDate periodDayDate = DateUtils.getPeriodDate(processingDate, periodAdjustedDay).withDayOfMonth(periodAdjustedDay);
        LocalDate periodAdjustedDayDate = periodAdjustedDay == 1 ? periodDayDate : periodDayDate.minusMonths(1);
        DateTime startDate = new DateTime(Date.from(periodAdjustedDayDate.atStartOfDay(CET).toInstant()));

        final ListenableFuture<List<Transaction>> transactions = submitIo(userId, priority, loadTransactionsTimer,
                () -> transactionDao
                        .findAllByUserIdAndTime(userId,
                                startDate,
                                DateTime.now().plusYears(1)).stream()
                        .filter(t -> t.getOriginalDate().after(startDate.toDate()))
                        .collect(Collectors.toList()));
        final ListenableFuture<List<Account>> accounts = submitIo(userId, priority, loadAccountsTimer,
                () -> accountRepository.findByUserId(userId));
        final ListenableFuture<List<AccountBalance>> accountBalances = submitIo(userId, priority,
                loadAccountBalancesTimer,
                () -> accountBalanceHistoryRepository.findByUserId(userId));
        final ListenableFuture<List<Credentials>> credentials = submitIo(userId, priority, loadCredentialsTimer,
                () -> credentialsRepository.findAllByUserId(userId));
        final ListenableFuture<UserState> userState = ioThreadPool
                .submit(new PrioritizedCallable<>(priority, loadUserStateTimer.wrap(
                        new NamedCallable<>(
                                () -> userStateRepository.findOneByUserId(userId),
                                String.format(TRANSACTION_PROCESSOR_IO_THREAD_BY_STRING, userId)
                        ))));
        final ListenableFuture<List<Property>> properties = submitIo(userId, priority, loadPropertiesTimer,
                () -> propertyRepository.findByUserId(userId));

        Optional<ListenableFuture<ImmutableListMultimap<String, Loan>>> loans = Optional.empty();
        if (Objects.equal(Cluster.TINK, serviceContext.getConfiguration().getCluster())) {
            loans = Optional.of(ioThreadPool
                    .submit(new PrioritizedCallable<>(priority, loanDataTimer.decorateMultimap(new NamedCallable<>(
                            () -> loanDataRepository.findAllByAccounts(accounts.get()),
                            String.format(TRANSACTION_PROCESSOR_IO_THREAD_BY_STRING, userId)
                    ))))
            );
        }

        final UserData userData = new UserData();
        userData.setUser(user);
        userData.setTransactions(transactions.get());
        userData.setAccounts(accounts.get());
        userData.setAccountBalanceHistory(accountBalances.get());
        userData.setCredentials(credentials.get());
        userData.setUserState(userState.get());
        userData.setProperties(properties.get());

        if (loans.isPresent()) {
            userData.setLoanDataByAccount(loans.get().get());
        }

        timerContext.stop();

        return userData;
    }

    private <T> ListenableFuture<List<T>> submitIo(String userId, int priority, ListSizeBuckettedTimer timer,
            Callable<List<T>> r) {
        return ioThreadPool.submit(new PrioritizedCallable<>(priority, new NamedCallable<>(timer.decorate(r),
                String.format(TRANSACTION_PROCESSOR_IO_THREAD_BY_STRING, userId))));
    }

    public void warmUp(final ServiceContext context) {
        transactionProcessor.warmUp(context);
    }

    @Override
    public void start() throws Exception {
        BlockingQueue<WrappedRunnableListenableFutureTask<PrioritizedRunnable, ?>> cpuThreadPoolQueue = PriorityExecutorQueueFactory
                .cappedPriorityQueue(t -> t.getDelegate().priority, MAX_QUEUED_CPU_REQUESTS,
                        Predicates.not(IS_HIGH_PRIORITY_RUNNABLE));
        cpuThreadPoolQueue = new
                ElementMonitoredQueue<>(
                cpuThreadPoolQueue, PRIORITY_RUNNABLE_KEY_EXTRACTOR,
                metricRegistry, "transaction_processor_cpu_executor_queue", new MetricId.MetricLabels());

        cpuThreadPool = ListenableThreadPoolExecutor.builder(
                cpuThreadPoolQueue,
                new TypedThreadPoolBuilder(NBR_OF_CPU_THREADS, TRANSACTIONS_CPU_THREAD_FACTORY))
                .withMetric(metricRegistry, "transaction_processor_cpu_executor")
                .build();

        final BlockingQueue<WrappedCallableListenableFutureTask<PrioritizedCallable<?>, ?>> ioThreadPoolQueue = Queues
                .newLinkedBlockingQueue();

        ioThreadPool = ListenableThreadPoolSubmitter.builder(
                ioThreadPoolQueue,
                new TypedThreadPoolBuilder(0, IO_THREAD_FACTORY)
                        .withMaximumPoolSize(Integer.MAX_VALUE, 1, TimeUnit.MINUTES))
                .withMetric(metricRegistry, "transaction_processor_io_executor")
                .build();
    }

    @Override
    public void stop() throws Exception {
        if (ioThreadPool != null) {
            ExecutorServiceUtils.shutdownExecutor("TransactionProcessorWorker#ioThreadPool",
                    ioThreadPool, 30, TimeUnit.SECONDS);
            ioThreadPool = null;
        }

        if (cpuThreadPool != null) {
            ExecutorServiceUtils.shutdownExecutor("TransactionProcessorWorker#cpuThreadPool",
                    cpuThreadPool, 30, TimeUnit.SECONDS);
            cpuThreadPool = null;
        }
    }
}
