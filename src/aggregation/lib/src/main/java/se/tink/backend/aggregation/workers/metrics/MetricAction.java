package se.tink.backend.aggregation.workers.metrics;

import com.google.common.base.Preconditions;
import java.util.List;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.types.timers.Timer;

public class MetricAction implements MetricActionIface {
    private final AgentWorkerCommandMetricState state;
    private final MetricCacheLoader metricCacheLoader;
    private final MetricId metricPath;

    private Timer.Context timer;

    MetricAction(
            AgentWorkerCommandMetricState state,
            MetricCacheLoader metricCacheLoader,
            MetricId metricPath) {
        Preconditions.checkArgument(state != null, "No MetricState provided");
        Preconditions.checkArgument(metricCacheLoader != null, "No MetricLoader provided");

        this.state = state;
        this.metricCacheLoader = metricCacheLoader;
        this.metricPath = metricPath;
    }

    public void start() {
        Preconditions.checkState(timer == null, "MetricAction already in progress");

        timer = metricCacheLoader.getMetricRegistry().timer(metricPath.suffix("duration")).time();
        state.add(this);
    }

    public void start(List<? extends Number> metricBuckets) {
        Preconditions.checkState(timer == null, "MetricAction already in progress");

        timer =
                metricCacheLoader
                        .getMetricRegistry()
                        .timer(metricPath.suffix("duration"), metricBuckets)
                        .time();
        state.add(this);
    }

    /**
     * Stop action timer and ask AgentWorkerCommandMetricState to remove itself from ongoing actions
     */
    public void stop() {
        Preconditions.checkState(timer != null, "No active timer to stop");

        timer.stop();
        state.remove(this);
    }

    public void completed() {
        mark(Outcome.COMPLETED);
    }

    public void failed() {
        mark(Outcome.FAILED);
    }

    public void cancelled() {
        mark(Outcome.CANCELLED);
    }

    public void unavailable() {
        mark(Outcome.UNAVAILABLE);
    }

    private void mark(Outcome outcome) {
        metricCacheLoader
                .getMetricRegistry()
                .meter(metricPath.label("outcome", outcome.getMetricName()))
                .inc();
    }

    private enum Outcome {
        COMPLETED,
        FAILED,
        CANCELLED,
        UNAVAILABLE;

        private String getMetricName() {
            return name().toLowerCase();
        }
    }

    public MetricId getMetricId() {
        return this.metricPath;
    }
}
