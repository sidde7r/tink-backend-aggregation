package se.tink.backend.aggregation.workers;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.workers.ratelimit.RateLimitedExecutorService;
import se.tink.backend.common.concurrency.InstrumentedRunnable;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.NamedRunnable;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class AgentWorker implements Managed {
    public static final String DEFAULT_AGENT_PACKAGE_CLASS_PREFIX =
            "se.tink.backend.aggregation.agents";

    private static final AggregationLogger log = new AggregationLogger(AgentWorker.class);
    private static final int NUMBER_OF_THREADS = 1000;
    private static final int BANKID_ATTEMPTS = 90;
    private static final int SLACK_DURATION = 5;
    private static final int SINGLE_BANKID_SIGN_TIMEOUT_SECONDS = 2 * BANKID_ATTEMPTS + SLACK_DURATION;
    private static final MetricId AGGREGATION_OPERATION_TASKS_METRIC_NAME = MetricId
            .newId("aggregation_operation_tasks");
    private static final String MONITOR_THREAD_NAME_FORMAT = "agent-worker-operation-thread-%s";
    private final MetricRegistry metricRegistry;

    /**
     * The time in minutes we wait until we forcefully shut down all agent work. Wirst case scenario is that an
     * aggregation operation requires signing bankid two times.
     */
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 2 * SINGLE_BANKID_SIGN_TIMEOUT_SECONDS;
    private static final MetricId AGGREGATION_EXECUTOR_SERVICE_METRIC_NAME = MetricId
            .newId("aggregation_executor_service");

    private RateLimitedExecutorService rateLimitedExecutorService;
    private ListenableThreadPoolExecutor<Runnable> aggregationExecutorService;
    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("aggregation-worker-agent-thread-%d").build();

    public AgentWorker(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void start() throws Exception {
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> executorServiceQueue = Queues
                .newLinkedBlockingQueue();

        aggregationExecutorService = ListenableThreadPoolExecutor.builder(
                executorServiceQueue,
                new TypedThreadPoolBuilder(NUMBER_OF_THREADS, threadFactory))
                .withMetric(metricRegistry, "aggregation_executor_service")
                .build();

        rateLimitedExecutorService = new RateLimitedExecutorService(aggregationExecutorService, metricRegistry);

        rateLimitedExecutorService.start();
    }

    public RateLimitedExecutorService getRateLimitedExecutorService() {
        return rateLimitedExecutorService;
    }

    @Override
    public void stop() throws Exception {
        rateLimitedExecutorService.stop();

        ExecutorServiceUtils.shutdownExecutor("AggregationWorker#aggregationExecutorService",
                aggregationExecutorService, SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        aggregationExecutorService = null;
    }

    public void execute(AgentWorkerOperation operation) throws Exception {
        InstrumentedRunnable instrumentedRunnable = new InstrumentedRunnable(
                metricRegistry,
                "aggregation_operation_tasks",
                new MetricId.MetricLabels()
                        .add("provider", operation.getRequest().getProvider().getName())
                        .add("request_type", operation.getRequest().isManual() ? "manual" : "automatic"),
                operation);

        NamedRunnable namedRunnable = new NamedRunnable(instrumentedRunnable,
                String.format(MONITOR_THREAD_NAME_FORMAT,
                        operation.getRequest().getCredentials().getId()));

        if (operation.getRequest().isManual()) {
            // Don't rate limit manual requests
            aggregationExecutorService.execute(namedRunnable);
        } else {
            rateLimitedExecutorService.execute(namedRunnable, operation.getRequest().getProvider());
        }
        instrumentedRunnable.submitted();
    }

    public void executeAutomaticRefresh(AgentWorkerRefreshOperationCreatorWrapper agentWorkerOperationCreatorRunnable) throws Exception {

        InstrumentedRunnable instrumentedRunnable = new InstrumentedRunnable(
                metricRegistry,
                "aggregation_operation_tasks",
                new MetricId.MetricLabels()
                        .add("provider", agentWorkerOperationCreatorRunnable.getProviderName())
                        .add("request_type", "automatic"),
                agentWorkerOperationCreatorRunnable);

        NamedRunnable namedRunnable = new NamedRunnable(instrumentedRunnable,
                String.format(MONITOR_THREAD_NAME_FORMAT,
                        agentWorkerOperationCreatorRunnable.getCredentialsId()));

        rateLimitedExecutorService.execute(namedRunnable, agentWorkerOperationCreatorRunnable.getProvider());
        instrumentedRunnable.submitted();
    }
}
