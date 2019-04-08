package se.tink.backend.aggregation.nxgen.controllers.metrics;

import com.google.common.base.Preconditions;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public abstract class MetricAction {
    private final Credentials credentials;
    final MetricRegistry registry;
    final MetricId metricId;

    private Timer.Context timer;

    MetricAction(Credentials credentials, MetricRegistry registry, MetricId metricId) {
        this.registry = registry;
        this.credentials = credentials;
        this.metricId = metricId;
    }

    public void start() {
        Preconditions.checkState(timer == null, "MetricAction already in progress");

        timer = registry.timer(metricId.suffix("duration")).time();
    }

    public void start(List<? extends Number> metricBuckets) {
        Preconditions.checkState(timer == null, "MetricAction already in progress");

        timer = registry.timer(metricId.suffix("duration"), metricBuckets).time();
    }

    public void stop() {
        Preconditions.checkState(timer != null, "No active timer to stop");

        timer.stop();
    }

    public void completed() {
        mark(Outcome.COMPLETED);
    }

    public void failed() {
        mark(Outcome.FAILED);
    }

    private void mark(Outcome outcome) {
        registry.meter(
                        metricId.label("outcome", outcome.getMetricName())
                                .label("status", credentials.getStatus().name()))
                .inc();
    }

    private enum Outcome {
        COMPLETED,
        FAILED;

        private String getMetricName() {
            return name().toLowerCase();
        }
    }

    public MetricId getMetricId() {
        return this.metricId;
    }
}
