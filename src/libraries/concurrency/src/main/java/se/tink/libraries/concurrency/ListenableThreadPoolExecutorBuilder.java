package se.tink.libraries.concurrency;

import com.google.common.util.concurrent.FutureCallback;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import se.tink.libraries.concurrency.logger.exception.FutureUncaughtExceptionLogger;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public final class ListenableThreadPoolExecutorBuilder<T extends Runnable> {
    private static final MetricId DEFAULT_METRIC_ID = MetricId.newId("thread_pool_executor");
    private static final MetricId.MetricLabels QUEUED_LABEL =
            new MetricId.MetricLabels().add("event", "queued");
    private static final MetricId.MetricLabels STARTED_LABEL =
            new MetricId.MetricLabels().add("event", "started");
    private static final MetricId.MetricLabels FINISHED_LABEL =
            new MetricId.MetricLabels().add("event", "finished");

    private FutureCallback<Object> errorLoggingCallback = new FutureUncaughtExceptionLogger();
    private RejectedExecutionHandler<WrappedRunnableListenableFutureTask<T, ?>> rejectedHandler =
            new AbortPolicy<>();
    private BlockingQueue<WrappedRunnableListenableFutureTask<T, ?>> queue;
    private TypedThreadPoolBuilder threadPoolBuilder;

    private Optional<MetricRegistry> metricRegistry = Optional.empty();
    private MetricId metricId = DEFAULT_METRIC_ID;
    private String metricName = "";

    public ListenableThreadPoolExecutorBuilder(
            BlockingQueue<WrappedRunnableListenableFutureTask<T, ?>> queue,
            TypedThreadPoolBuilder tpb) {
        this.threadPoolBuilder = tpb;
        this.queue = queue;
    }

    public ListenableThreadPoolExecutorBuilder<T> withRejectedHandler(
            RejectedExecutionHandler<WrappedRunnableListenableFutureTask<T, ?>> rejectedHandler) {
        this.rejectedHandler = rejectedHandler;
        return this;
    }

    public ListenableThreadPoolExecutorBuilder<T> withMetric(
            MetricRegistry metricRegistry, String metricName) {
        this.metricRegistry = Optional.of(metricRegistry);
        this.metricName = metricName;
        return this;
    }

    public ListenableThreadPoolExecutorBuilder<T> withMetricLabels(MetricId.MetricLabels labels) {
        this.metricId = metricId.label(labels);
        return this;
    }

    public ListenableThreadPoolExecutorBuilder<T> withErrorLoggingCallback(
            FutureCallback<Object> errorLoggingCallback) {
        this.errorLoggingCallback = errorLoggingCallback;
        return this;
    }

    /** Starts a background thread on instantiation. Must call `#shutdown` when done. */
    public ListenableThreadPoolExecutor<T> build() {
        MetricId metric = metricId.label("name", metricName);
        ListenableThreadPoolExecutor<T> listenableThreadPoolExecutor =
                new ListenableThreadPoolExecutor<>(
                        rejectedHandler,
                        queue,
                        threadPoolBuilder,
                        errorLoggingCallback,
                        metricRegistry
                                .map(r -> r.meter(metric.label(QUEUED_LABEL)))
                                .orElseGet(Counter::new),
                        metricRegistry
                                .map(r -> r.meter(metric.label(STARTED_LABEL)))
                                .orElseGet(Counter::new),
                        metricRegistry
                                .map(r -> r.meter(metric.label(FINISHED_LABEL)))
                                .orElseGet(Counter::new));

        return listenableThreadPoolExecutor;
    }
}
