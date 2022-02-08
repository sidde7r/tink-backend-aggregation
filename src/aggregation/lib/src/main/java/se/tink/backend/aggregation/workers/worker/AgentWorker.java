package se.tink.backend.aggregation.workers.worker;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation;
import se.tink.backend.aggregation.workers.ratelimit.RateLimitedExecutorService;
import se.tink.libraries.concurrency.InstrumentedRunnable;
import se.tink.libraries.concurrency.ListenableThreadPoolExecutor;
import se.tink.libraries.concurrency.NamedRunnable;
import se.tink.libraries.concurrency.TypedThreadPoolBuilder;
import se.tink.libraries.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.dropwizard_lifecycle.ManagedSafeStop;
import se.tink.libraries.executor.ExecutorServiceUtils;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.tracing.lib.api.Tracing;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

public class AgentWorker extends ManagedSafeStop {
    private static final Logger log = LoggerFactory.getLogger(AgentWorker.class);
    private static final int NUMBER_OF_THREADS = 1000;
    private static final String MONITOR_THREAD_NAME_FORMAT = "agent-worker-operation-thread-%s";
    // On Leeds (running 3g heap size), we started GC:ing aggressively when above 180k elements in
    // the queue here. At
    // 300k elements we ran out of memory entirely and all aggregation deadlocked (note that they
    // did not restart). The
    // reason we queued up was because our rate limitters were limitting us to process at the
    // incoming rate. That
    // said, we should not be piling up this many requests on an aggregation instance.
    //
    // If we hit this limit, #submit and #execute will throw RejectedExecutionException.
    private static final int MAX_QUEUED_UP = 180000;

    // Automatic Refreshes will be put on a persistent queue. This queue will be used as a buffer
    // only.
    private static final int MAX_QUEUE_AUTOMATIC_REFRESH = 200;

    /**
     * As of right now, the longest supplemental information timeout is 540 seconds. This is the max
     * we have to wait in order for the call to timeout properly. This is required, as otherwise
     * System and other downstreams consumers of the credentials update won't get the state
     * transition (with metrics). See ThirdPartyAppConstants.java for the current value.
     */
    private static final long LONGEST_SUPPLEMENTAL_INFORMATION_SECONDS =
            TimeUnit.MINUTES.toSeconds(ThirdPartyAppConstants.WAIT_FOR_MINUTES);
    /**
     * As Kubernetes will signal with SIGTERM and then start the process of removing the Aggregation
     * Instance from the internal load-balancer, we need to add some slack in order for the instance
     * to not hit the above timeout before we terminate the instance.
     */
    private static final Duration NEW_AGGREGATION_SLACK = Duration.ofSeconds(60);
    /**
     * The time in minutes we wait until we forcefully shut down all agent work. This is a
     * combination of the above two numbers.
     */
    private static final long SHUTDOWN_TIMEOUT_SECONDS = LONGEST_SUPPLEMENTAL_INFORMATION_SECONDS;

    private static final ThreadFactory threadFactory =
            new ThreadFactoryBuilder().setNameFormat("aggregation-worker-agent-thread-%d").build();

    private final UnleashClient unleashClient;
    private final MetricRegistry metricRegistry;
    private final boolean queueAvailable;

    private RateLimitedExecutorService rateLimitedExecutorService;
    private RateLimitedExecutorService automaticRefreshRateLimitedExecutorService;
    private ListenableThreadPoolExecutor<Runnable> aggregationExecutorService;
    private ListenableThreadPoolExecutor<Runnable> automaticRefreshExecutorService;

    @Inject
    public AgentWorker(
            UnleashClient unleashClient,
            MetricRegistry metricRegistry,
            @Named("queueAvailable") boolean queueAvailable) {
        this.unleashClient = unleashClient;
        this.metricRegistry = metricRegistry;
        this.queueAvailable = queueAvailable;
    }

    @Override
    public void start() throws Exception {
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> executorServiceQueue =
                Queues.newLinkedBlockingQueue();

        aggregationExecutorService =
                ListenableThreadPoolExecutor.builder(
                                executorServiceQueue,
                                new TypedThreadPoolBuilder(NUMBER_OF_THREADS, threadFactory))
                        .withMetric(metricRegistry, "aggregation_executor_service")
                        .build();

        rateLimitedExecutorService =
                new RateLimitedExecutorService(
                        aggregationExecutorService, metricRegistry, MAX_QUEUED_UP);
        rateLimitedExecutorService.start();

        // Build execution services for automatic refreshes
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>>
                automaticExecutorServiceQueue =
                        Queues.newLinkedBlockingQueue(
                                queueAvailable ? MAX_QUEUE_AUTOMATIC_REFRESH : MAX_QUEUED_UP);

        automaticRefreshExecutorService =
                ListenableThreadPoolExecutor.builder(
                                automaticExecutorServiceQueue,
                                new TypedThreadPoolBuilder(NUMBER_OF_THREADS, threadFactory))
                        .withMetric(
                                metricRegistry, "automatic_refresh_aggregation_executor_service")
                        .build();

        automaticRefreshRateLimitedExecutorService =
                new RateLimitedExecutorService(
                        automaticRefreshExecutorService,
                        metricRegistry,
                        queueAvailable ? MAX_QUEUE_AUTOMATIC_REFRESH : MAX_QUEUED_UP);
        automaticRefreshRateLimitedExecutorService.start();
    }

    public RateLimitedExecutorService getRateLimitedExecutorService() {
        return rateLimitedExecutorService;
    }

    @Override
    public void doStop() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.info(
                "Sleeping for {}ms, to ensure that we get all new operations before we start the countdown",
                NEW_AGGREGATION_SLACK.toMillis());
        Thread.sleep(NEW_AGGREGATION_SLACK.toMillis());
        log.info("Initiated shutdown of thread pools");
        rateLimitedExecutorService.stop();
        if (unleashClient.isToggleEnabled(Toggle.of("closing-bgr-refresh-executor").build())) {
            log.info("Parallel shutdown of executors");
            automaticRefreshRateLimitedExecutorService.stop();
            shutdownExecutorsInParallel();
        } else {
            log.info("Shutdown of aggregation executor service");
            shutdownAggregationExecutorService();
        }

        log.info("Shutdown took {} seconds", stopwatch.elapsed(TimeUnit.SECONDS));
    }

    public void execute(AgentWorkerOperation operation) throws Exception {
        log.debug("[AgentWorker] Starting executing");
        CredentialsRequest request = operation.getRequest();
        InstrumentedRunnable instrumentedRunnable =
                new InstrumentedRunnable(
                        metricRegistry,
                        "aggregation_operation_tasks",
                        new MetricId.MetricLabels()
                                .add("provider", request.getProvider().getName())
                                .add("market", request.getProvider().getMarket())
                                .add(
                                        "request_type",
                                        request.getUserAvailability().isUserPresent()
                                                ? "manual"
                                                : "automatic"),
                        operation);

        NamedRunnable namedRunnable =
                new NamedRunnable(
                        instrumentedRunnable,
                        String.format(
                                MONITOR_THREAD_NAME_FORMAT, request.getCredentials().getId()));

        if (request.getUserAvailability().isUserPresent()) {
            // Don't rate limit manual requests
            try {
                aggregationExecutorService.execute(Tracing.wrapRunnable(namedRunnable));
            } catch (RuntimeException e) {
                log.error("[AgentWorker] Failed to execute using aggregationExecutorService", e);
                throw e;
            }
        } else {
            try {
                rateLimitedExecutorService.execute(namedRunnable, request.getProvider());
            } catch (RuntimeException e) {
                log.error("[AgentWorker] Failed to execute using rateLimitedExecutorService", e);
                throw e;
            }
        }
        instrumentedRunnable.submitted();
        log.debug("[AgentWorker] Finished scheduling");
    }

    public void executeAutomaticRefresh(
            AgentWorkerRefreshOperationCreatorWrapper agentWorkerOperationCreatorRunnable)
            throws Exception {
        log.debug("[AgentWorker] Starting executing automatic refresh");
        InstrumentedRunnable instrumentedRunnable =
                new InstrumentedRunnable(
                        metricRegistry,
                        "aggregation_operation_tasks",
                        new MetricId.MetricLabels()
                                .add(
                                        "provider",
                                        agentWorkerOperationCreatorRunnable.getProviderName())
                                .add(
                                        "market",
                                        agentWorkerOperationCreatorRunnable
                                                .getProvider()
                                                .getMarket())
                                .add("request_type", "automatic"),
                        agentWorkerOperationCreatorRunnable);

        NamedRunnable namedRunnable =
                new NamedRunnable(
                        instrumentedRunnable,
                        String.format(
                                MONITOR_THREAD_NAME_FORMAT,
                                agentWorkerOperationCreatorRunnable.getCredentialsId()));

        try {
            automaticRefreshRateLimitedExecutorService.execute(
                    namedRunnable, agentWorkerOperationCreatorRunnable.getProvider());
        } catch (RuntimeException e) {
            log.debug(
                    "[AgentWorker] Failed to execute using automaticRefreshRateLimitedExecutorService",
                    e);
            throw e;
        }

        instrumentedRunnable.submitted();
        log.debug("[AgentWorker] Finished scheduling automatic refresh");
    }

    private void shutdownExecutorsInParallel() {
        ExecutorService shutdownExecutor = Executors.newFixedThreadPool(2);
        shutdownExecutor.execute(this::shutdownAutomaticRefreshExecutorService);
        shutdownExecutor.execute(this::shutdownAggregationExecutorService);
        shutdown(shutdownExecutor);
        log.info("[AgentWorker] shutdownExecutor - successful shutdown");
    }

    private void shutdown(ExecutorService shutdownExecutor) {
        shutdownExecutor.shutdown();
        try {
            if (!shutdownExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                shutdownExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            shutdownExecutor.shutdownNow();
            log.warn("Shutdown was interrupted while awaiting termination.", e);
            Thread.currentThread().interrupt();
        }
        log.info("[AgentWorker] shutdownExecutor - successful shutdown");
    }

    private void shutdownAutomaticRefreshExecutorService() {
        shutdownExecutor(
                "AggregationWorker#automaticRefreshExecutorService",
                automaticRefreshExecutorService);
        automaticRefreshExecutorService = null;
    }

    private void shutdownAggregationExecutorService() {
        shutdownExecutor(
                "AggregationWorker#aggregationExecutorService", aggregationExecutorService);
        aggregationExecutorService = null;
    }

    private void shutdownExecutor(
            String executorName,
            ListenableThreadPoolExecutor<Runnable> automaticRefreshExecutorService) {
        ExecutorServiceUtils.shutdownExecutor(
                executorName,
                automaticRefreshExecutorService,
                SHUTDOWN_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
    }
}
