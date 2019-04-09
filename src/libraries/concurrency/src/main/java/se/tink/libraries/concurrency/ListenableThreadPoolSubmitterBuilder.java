package se.tink.libraries.concurrency;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class ListenableThreadPoolSubmitterBuilder<T extends Callable<?>> {
    private static final MetricId DEFAULT_METRIC_ID = MetricId.newId("thread_pool");
    private static final MetricId.MetricLabels QUEUED_LABEL =
            new MetricId.MetricLabels().add("event", "queued");
    private static final MetricId.MetricLabels STARTED_LABEL =
            new MetricId.MetricLabels().add("event", "started");
    private static final MetricId.MetricLabels FINISHED_LABEL =
            new MetricId.MetricLabels().add("event", "finished");

    private BlockingQueue<WrappedCallableListenableFutureTask<T, ?>> queue;
    private TypedThreadPoolBuilder threadPoolBuilder;
    private Optional<MetricRegistry> metricRegistry = Optional.empty();
    private RejectedExecutionHandler<WrappedCallableListenableFutureTask<T, ?>> rejectedHandler =
            new AbortPolicy<>();
    private String metricName = "";
    private MetricId metricId = DEFAULT_METRIC_ID;

    public ListenableThreadPoolSubmitterBuilder(
            BlockingQueue<WrappedCallableListenableFutureTask<T, ?>> queue,
            TypedThreadPoolBuilder threadPoolBuilder) {
        this.queue = queue;
        this.threadPoolBuilder = threadPoolBuilder;
    }

    public ListenableThreadPoolSubmitterBuilder<T> withMetric(
            MetricRegistry metricRegistry, String metricName) {
        this.metricRegistry = Optional.of(metricRegistry);
        this.metricName = metricName;
        return this;
    }

    public ListenableThreadPoolSubmitterBuilder<T> withMetricLabels(MetricId.MetricLabels labels) {
        this.metricId = metricId.label(labels);
        return this;
    }

    /** Starts a background thread on instantiation. Must call `#shutdown` when done. */
    public ListenableThreadPoolSubmitter<T> build() {
        MetricId metric = metricId.label("name", metricName);
        return new ListenableThreadPoolSubmitter<>(
                queue,
                threadPoolBuilder,
                rejectedHandler,
                metricRegistry
                        .map(r -> r.meter(metric.label(QUEUED_LABEL)))
                        .orElseGet(Counter::new),
                metricRegistry
                        .map(r -> r.meter(metric.label(STARTED_LABEL)))
                        .orElseGet(Counter::new),
                metricRegistry
                        .map(r -> r.meter(metric.label(FINISHED_LABEL)))
                        .orElseGet(Counter::new));
    }
}
