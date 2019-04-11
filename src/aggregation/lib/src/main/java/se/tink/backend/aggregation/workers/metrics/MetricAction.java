package se.tink.backend.aggregation.workers.metrics;

import com.google.common.base.Preconditions;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.Timer;

public class MetricAction {
    private final AgentWorkerCommandMetricState state;
    private final MetricCacheLoader metricCacheLoader;
    private final MetricId metricPath;
    private final Credentials credentials;

    private Timer.Context timer;

    MetricAction(
            AgentWorkerCommandMetricState state,
            MetricCacheLoader metricCacheLoader,
            Credentials credentials,
            MetricId metricPath) {
        Preconditions.checkArgument(state != null, "No MetricState provided");
        Preconditions.checkArgument(metricCacheLoader != null, "No MetricLoader provided");

        this.state = state;
        this.metricCacheLoader = metricCacheLoader;
        this.credentials = credentials;
        this.metricPath = metricPath;
    }

    public void start() {
        Preconditions.checkState(timer == null, "MetricAction already in progress");

        timer = metricCacheLoader.startTimer(metricPath.suffix("duration"));
        state.add(this);
    }

    public void start(List<? extends Number> metricBuckets) {
        Preconditions.checkState(timer == null, "MetricAction already in progress");

        timer = metricCacheLoader.startTimer(metricPath.suffix("duration"), metricBuckets);
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
        metricCacheLoader.mark(metricPath.label("outcome", outcome.getMetricName()));
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
