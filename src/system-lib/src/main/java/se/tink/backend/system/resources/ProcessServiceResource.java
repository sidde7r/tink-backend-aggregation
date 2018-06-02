package se.tink.backend.system.resources;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Provider;
import io.dropwizard.lifecycle.Managed;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.joda.time.DateTime;
import se.tink.backend.categorization.client.FastTextServiceFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.ElementMonitoredQueue;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.NamedRunnable;
import se.tink.backend.common.concurrency.PrioritizedRunnable;
import se.tink.backend.common.concurrency.PriorityExecutorQueueFactory;
import se.tink.backend.common.concurrency.StatisticsActivitiesLock;
import se.tink.backend.common.concurrency.TransactionInMemoryReadWriteLock;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.properties.RefreshPropertiesController;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.common.statistics.StatisticQueryExecutor;
import se.tink.backend.common.tasks.kafka.KafkaQueueResetter;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.CassandraStatistic;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticContainer;
import se.tink.backend.core.StatisticGenerationMode;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.insights.client.InsightsServiceFactory;
import se.tink.backend.insights.http.dto.CreateInsightsRequest;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.system.SystemServiceResource;
import se.tink.backend.system.api.ProcessService;
import se.tink.backend.system.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.system.rpc.ProcessTransactionsRequest;
import se.tink.backend.system.rpc.ReplayQueueRequest;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.UpdateTransactionsRequest;
import se.tink.backend.system.statistics.StatisticsTransformer;
import se.tink.backend.system.workers.activity.ActivityGeneratorWorker;
import se.tink.backend.system.workers.processor.TransactionProcessorWorker;
import se.tink.backend.system.workers.processor.chaining.DefaultUserChainFactoryCreator;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.system.workers.statistics.StatisticsGeneratorWorker;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.jersey.logging.UserRuntimeException;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.SequenceTimer;
import se.tink.libraries.metrics.SequenceTimers;

@Path("/process")
public class ProcessServiceResource implements ProcessService, Managed {
    private static final ThreadFactory transactionsThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("transaction-processor-thread-%d")
            .build();

    private static final Function<WrappedRunnableListenableFutureTask<PrioritizedRunnable, ?>, String>
            PRIORITY_RUNNABLE_KEY_EXTRACTOR = Functions
            .compose(new ElementMonitoredQueue.PrioritizedRunnableLabelExtractor(),
                    new WrappedRunnableListenableFutureTask.DelegateExtractor<>());
    private final RefreshPropertiesController refreshPropertiesController;
    private final ServiceConfiguration serviceConfiguration;
    private final UserRepository userRepository;
    private static final ThreadFactory statisticsThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("statistics-generator-thread-%d")
            .build();
    private final DefaultUserChainFactoryCreator chainFactoryCreator;

    private static class Timers {
        private static final String PREPARE = "prepare";
        private static final String PROCESS_ASYNC = "process-async";
        private static final String PROCESS_SYNC = "process-sync";
    }

    private static final int MAX_QUEUED_STATISTICS_GENERATION_REQUESTS = 200;
    private static final int STATISTICS_GENERATOR_NTHREADS = 5;
    private static final String STATISTICS_THREAD_NAME_FORMAT = "statistics-generation-thread-%s";

    private final CuratorFramework coordinationClient;

    private ListenableThreadPoolExecutor<PrioritizedRunnable> statisticsGeneratorExecutorService;

    private final SequenceTimer updateTransactionsSequenceTimer;
    private final TransactionProcessorWorker transactionProcessorWorker;
    private final ActivityGeneratorWorker activityGeneratorWorker;
    private final StatisticsGeneratorWorker statisticsGeneratorWorker;
    private final StatisticQueryExecutor statisticQueryExecutor;
    private final FirehoseQueueProducer firehoseQueueProducer;
    private final MetricRegistry metricRegistry;
    private final static LogUtils log = new LogUtils(ProcessServiceResource.class);

    private final InsightsServiceFactory insightsServiceFactory;

    private KafkaQueueResetter queueResetter;

    // TODO: Use Guice injection here.
    public ProcessServiceResource(ServiceContext serviceContext,
            FirehoseQueueProducer firehoseQueueProducer,
            KafkaQueueResetter queueResetter,
            Provider<MarketDescriptionFormatterFactory> descriptionFormatterFactory,
            Provider<MarketDescriptionExtractorFactory> descriptionExtractorFactory,
            MetricRegistry metricRegistry, ClusterCategories categories, FastTextServiceFactory fastTextServiceFactory,
            ElasticSearchClient elasticSearchClient, InsightsServiceFactory insightsServiceFactory,
            DefaultUserChainFactoryCreator chainFactoryCreator) {

        this.firehoseQueueProducer = firehoseQueueProducer;
        this.queueResetter = queueResetter;

        this.coordinationClient = serviceContext.getCoordinationClient();
        this.metricRegistry = metricRegistry;

        this.updateTransactionsSequenceTimer = new SequenceTimer(SystemServiceResource.class, this.metricRegistry,
                SequenceTimers.UPDATE_TRANSACTIONS);

        // Setup the various workers.

        this.chainFactoryCreator = chainFactoryCreator;

        this.transactionProcessorWorker = new TransactionProcessorWorker(
                serviceContext,
                chainFactoryCreator,
                metricRegistry
        );
        this.statisticsGeneratorWorker = new StatisticsGeneratorWorker(serviceContext, firehoseQueueProducer,
                metricRegistry);
        this.statisticQueryExecutor = new StatisticQueryExecutor(serviceContext.getConfiguration().getStatistics());
        this.activityGeneratorWorker = new ActivityGeneratorWorker(this.metricRegistry, serviceContext,
                new DeepLinkBuilderFactory(serviceContext.getConfiguration().getNotifications().getDeepLinkPrefix()),
                firehoseQueueProducer, elasticSearchClient);

        this.serviceConfiguration = serviceContext.getConfiguration();

        this.userRepository = serviceContext.getRepository(UserRepository.class);

        this.refreshPropertiesController = new RefreshPropertiesController(
                serviceContext.getRepository(PropertyRepository.class),
                serviceContext.getRepository(FraudDetailsRepository.class),
                serviceContext.getRepository(AccountRepository.class),
                serviceContext.getRepository(LoanDataRepository.class),
                serviceContext.getSystemServiceFactory(),
                () -> DateTime.now().toDate());

        this.insightsServiceFactory = insightsServiceFactory;
    }

    public void warmUp(ServiceContext context) {
        transactionProcessorWorker.warmUp(context);
    }

    @Override
    public void start() throws Exception {
        BlockingQueue<WrappedRunnableListenableFutureTask<PrioritizedRunnable, ?>> statisticsGeneratorExecutorServiceQueue = PriorityExecutorQueueFactory
                .cappedPriorityQueue(t -> t.getDelegate().priority, MAX_QUEUED_STATISTICS_GENERATION_REQUESTS,
                        com.google.common.base.Predicates.<PrioritizedRunnable>alwaysFalse());
        statisticsGeneratorExecutorServiceQueue = new
                ElementMonitoredQueue<>(
                statisticsGeneratorExecutorServiceQueue, PRIORITY_RUNNABLE_KEY_EXTRACTOR, metricRegistry,
                "statistics_generator_executor_service_queue", new MetricId.MetricLabels());

        statisticsGeneratorExecutorService = ListenableThreadPoolExecutor.builder(
                statisticsGeneratorExecutorServiceQueue,
                new TypedThreadPoolBuilder(STATISTICS_GENERATOR_NTHREADS, statisticsThreadFactory))
                .withMetric(metricRegistry, "statistics_generator_executor_service")
                .build();

        transactionProcessorWorker.start();
    }

    @Override
    public void stop() throws Exception {
        transactionProcessorWorker.stop();
        statisticsGeneratorWorker.stop();

        // Transaction processor executor submits Runnables to this one. Important this is shut down _after_
        // transactionProcessorExecutorService has been drained.
        if (statisticsGeneratorExecutorService != null) {
            ExecutorServiceUtils.shutdownExecutor("ProcessServiceResource#statisticsGeneratorExecutorService",
                    statisticsGeneratorExecutorService, 10, TimeUnit.SECONDS);
            statisticsGeneratorExecutorService = null;
        }

        chainFactoryCreator.close();
    }

    @Override
    public Response updateTransactionsAsynchronously(UpdateTransactionsRequest request) {
        return updateTransactions(request, true, false);
    }

    @Override
    public Response updateTransactionsSynchronously(UpdateTransactionsRequest request) {
        return updateTransactions(request, false, true);
    }

    private Response updateTransactions(UpdateTransactionsRequest request, final boolean async,
            final boolean rethrowAllExceptions) {

        if (request.getTransactions().size() == 0) {
            log.warn(request.getUser(), request.getCredentials(), "Not updating empty transaction set");
            return HttpResponseHelper.ok();
        }

        final SequenceTimer.Context updateTransactionsSequenceTimerContext = updateTransactionsSequenceTimer.time();

        log.info(request.getCredentials(), "Updating transactions: " + request.getTransactions().size());

        updateTransactionsSequenceTimerContext.mark(Timers.PREPARE);

        // Same date is used for all transactions in the same batch
        long inserted = System.currentTimeMillis();

        for (Transaction transaction : request.getTransactions()) {
            transaction.setInserted(inserted);
            transaction.setTimestamp(System.currentTimeMillis());
            transaction.setOriginalDescription(CharMatcher.WHITESPACE.trimFrom(Strings.nullToEmpty(transaction
                    .getDescription())));
            transaction.setOriginalAmount(transaction.getAmount());
            transaction.setOriginalDate(transaction.getDate());
        }

        final ProcessTransactionsRequest processTransactionsRequest = new ProcessTransactionsRequest();

        processTransactionsRequest.setTopic(request.getTopic());
        processTransactionsRequest.setUserId(request.getUser());
        processTransactionsRequest.setCredentialsId(request.getCredentials());
        processTransactionsRequest.setTransactions(request.getTransactions());

        // If the user is in the app, or this is due to a context poll, we shouldn't send out notifications.
        processTransactionsRequest.setUserTriggered(request.isUserTriggered());

        updateTransactionsSequenceTimerContext.mark(async ? Timers.PROCESS_ASYNC : Timers.PROCESS_SYNC);

        try {
            ListenableFuture<?> future = transactionProcessorWorker
                    .process(processTransactionsRequest, rethrowAllExceptions);
            future.addListener(updateTransactionsSequenceTimerContext::stop, MoreExecutors.directExecutor());

            if (!async) {
                try {
                    Uninterruptibles.getUninterruptibly(future, 10, TimeUnit.MINUTES);
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (RejectedExecutionException e) {
            updateTransactionsSequenceTimerContext.stop();
            // We are overloaded and have too many tasks queued up. See #MAX_QUEUED_TRANSACTION_REQUESTS.
            HttpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return HttpResponseHelper.ok();
    }

    @Override
    public Response generateStatisticsAndActivityAsynchronously(GenerateStatisticsAndActivitiesRequest request) {
        generateStatisticsAndActivities(request, true);
        return HttpResponseHelper.ok();
    }

    @Override
    public Response generateStatisticsAndActivitySynchronous(GenerateStatisticsAndActivitiesRequest request) {
        generateStatisticsAndActivities(request, false);
        return HttpResponseHelper.ok();
    }

    @Override
    public void generateStatisticsAndActivitiesWithoutNotifications(String userId, StatisticMode mode) {
        GenerateStatisticsAndActivitiesRequest statisticsRequest = new GenerateStatisticsAndActivitiesRequest();

        statisticsRequest.setUserId(userId);
        statisticsRequest.setMode(mode);
        statisticsRequest.setUserTriggered(true);

        generateStatisticsAndActivityAsynchronously(statisticsRequest);
    }

    @Override
    public void generateProperties(String userId) {
        refreshPropertiesController.refresh(userRepository.findOne(userId));
    }

    @Override
    public Response resetConnectorQueues(ReplayQueueRequest request) {

        Preconditions.checkArgument(request.getStartReplayDate() == null);

        // 10 min safety
        // Meaning queue vs system instance clock mismatch < 10m is OK.
        // Also means that we will replay between from and to date but also always last minute.
        Date resetStartDate = DateUtils.addMinutes(new Date(), -10);

        if (request.getToDate() == null) {
            request.setToDate(resetStartDate);
        }

        if (request.getFromDate().after(resetStartDate) || request.getToDate().after(resetStartDate)) {
            // Future date, don't allow this
            log.warn("We don't allow future dates");
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        if (request.getFromDate().after(request.getToDate())) {
            // Future date, don't allow this
            log.warn("'fromDate' must be before 'toDate'");
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        request.setStartReplayDate(resetStartDate);
        Response.Status status = queueResetter.register(request) ? Response.Status.OK : Response.Status.CONFLICT;

        return Response.status(status).build();
    }

    private void generateStatisticsAndActivities(final GenerateStatisticsAndActivitiesRequest request, boolean async) {
        log.info(request.getUserId(), request.getCredentialsId(), "Generating statistics and activities");

        StatisticsActivitiesLock lock = new StatisticsActivitiesLock(coordinationClient, request.getUserId());

        lock.prepareForGeneration();

        log.debug(request.getUserId(), request.getCredentialsId(), "Submitting to thread pool");

        final int priority = request.isUserTriggered() ? PrioritizedRunnable.HIGH_PRIORITY
                : PrioritizedRunnable.LOW_PRIORITY;
        final Runnable runnable = () -> {
            try {
                generateStatisticsAndActivities(request);
            } catch (Exception e) {
                throw new UserRuntimeException(request.getUserId(), "Could not generate statistics and activities", e);
            }
        };

        final Runnable namedRunnable = new NamedRunnable(runnable, String.format(STATISTICS_THREAD_NAME_FORMAT,
                request.getCredentialsId()));

        if (async) {

            try {
                // Important we call #execute here and not #submit, which wraps in a FutureRunnable, which is not
                // comparable.
                statisticsGeneratorExecutorService.execute(new PrioritizedRunnable(priority, namedRunnable));
            } catch (RejectedExecutionException e) {
                // We are overloaded and have too many tasks queued up. See #MAX_QUEUED_STATISTICS_GENERATION_REQUESTS.
                HttpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE);
            }

        } else {

            namedRunnable.run();

        }
    }

    private void generateStatisticsAndActivities(GenerateStatisticsAndActivitiesRequest request) throws Exception {

        StatisticsActivitiesLock lock = new StatisticsActivitiesLock(coordinationClient, request.getUserId());
        InterProcessMutex transactionLock = new TransactionInMemoryReadWriteLock(
                coordinationClient, request.getUserId()).getLockForHoldingTransactionsInMemory();

        try {

            lock.lockForGeneration();
            if (request.isTakeReadlock()) {
                transactionLock.acquire();
            }

            UserData userData = statisticsGeneratorWorker.loadUserData(request);

            if (!doesUserExist(request, userData)) {
                return;
            }

            statisticsGeneratorWorker.generateStatistics(request);

            // Save generated statistics to cache.
            final StatisticContainer statisticsContainer = createStatisticsContainer(userData.getStatistics(),
                    userData.getUser().getId());

            List<CassandraStatistic> cassandraStatistics = StatisticsTransformer.transform(statisticsContainer);

            statisticsGeneratorWorker.saveStatisticsToDatabase(userData.getUser().getId(), request.getCredentialsId(),
                    userData.getUser().getFlags(), cassandraStatistics, statisticsContainer);
            
            statisticsGeneratorWorker.invalidateStatisticsCache(userData.getUser().getId(), request.getCredentialsId(),
                    userData.getUser().getFlags());

            statisticsGeneratorWorker.saveCredentialsToDb(request);
            statisticsGeneratorWorker.generateUserState(request.getMode(), userData, serviceConfiguration.getCluster());
            statisticsGeneratorWorker.updateContextTimestamp(request.getUserId());

            ActivityGeneratorContext activityContext = null;


                if (isValidForActivities(userData)) {

                    log.info(request.getUserId(), "Generating activities");

                    activityContext = activityGeneratorWorker.generateActivityContext(userData);

                    activityGeneratorWorker.generateActivities(userData, activityContext);
                }

            if (Objects.equal(serviceConfiguration.getCluster(), Cluster.TINK)) {
                // Residences need to be refreshed if loan data or fraud data has been updated, in which case the
                // generation mode is set to `FULL`. Since the fraud data is processed and persisted in the activity
                // generator, this needs to be done _after_ `activityGeneratorWorker.generateActivities(...)`.
                // If the residences are changed in the refresh, it will invoke another statistics and activity
                // generation, so dependencies on residences in the generation will be fulfilled in the second pass.
                if (Objects.equal(StatisticMode.FULL, request.getMode())) {
                    userData.setProperties(refreshPropertiesController.refresh(userData.getUser()));
                }
            }

            // Release the read lock so clients can get statistics and activities from cache.
            lock.releaseForRead();

            if (activityContext != null) {
                activityGeneratorWorker.saveActivitiesToDatabase(request.getUserId(), activityContext.getActivities());
                activityGeneratorWorker.sendActivitiesToFirehose(activityContext, request.isUserTriggered());
                activityGeneratorWorker.processNotifications(activityContext, request.isUserTriggered());
            }


            // If the insight service is enabled.
            // Only for users that have the feature flag.
            if (insightsServiceFactory != null) {
                User user = userRepository.findOne(request.getUserId());
                if (user != null && user.getFlags().contains(FeatureFlags.TEST_ACTIONABLE_INSIGHTS)) {
                    insightsServiceFactory.getInsightsService().create(new CreateInsightsRequest(user.getId()));
                }
            }
        } finally {
            if (request.isTakeReadlock() && transactionLock.isAcquiredInThisProcess()) {
                transactionLock.release();
            }
            lock.releaseAfterGeneration();
        }

        // It is better to send it in DAO level, to be sure, that we do not miss anything. Do it here, because of
        // firehose producer is running only in system container

        FirehoseMessage.Type messageType;

        if (request.getUserData().isPresent()) {
            messageType = request.getStatisticGenerationMode().getMessageType();

            firehoseQueueProducer.sendStatisticsMessage(request.getUserId(), messageType,
                    statisticQueryExecutor
                            .queryContextStatistic(request.getUserData().get().getUser().getProfile().getPeriodMode(),
                                    request.getUserData().get().getStatistics(), false));
        }
    }

    private StatisticContainer createStatisticsContainer(List<Statistic> statistics, String userId) {
        StatisticContainer statisticsContainer = new StatisticContainer(statistics);
        statisticsContainer.setUserId(userId);

        return statisticsContainer;
    }

    private boolean isValidForActivities(UserData userData) {
        if ((userData.getCredentials().isEmpty() || userData.getAccounts().isEmpty()
                || userData.getTransactions().isEmpty()) &&
                !FeatureFlags.FeatureFlagGroup.FRAUD_FEATURE.isFlagInGroup(userData.getUser().getFlags())) {

            if (userData.getCredentials().isEmpty()) {
                log.info(userData.getUser().getId(), "Not generating activities for user without credentials.");
            }

            if (userData.getAccounts().isEmpty()) {
                log.info(userData.getUser().getId(), "Not generating activities for user without accounts.");
            }

            if (userData.getTransactions().isEmpty()) {
                log.info(userData.getUser().getId(), "Not generating activities for user without transactions.");
            }

            return false;
        }
        return true;
    }

    private boolean doesUserExist(GenerateStatisticsAndActivitiesRequest request, UserData userData) {
        if (userData.getUser() == null) {
            log.warn(request.getUserId(),
                    "Could not find the User. Probably due to user being deleted concurrently. Not continuing.");
            return false;
        }
        return true;
    }
}
