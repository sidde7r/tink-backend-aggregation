package se.tink.backend.aggregation.workers.metrics;

import java.util.List;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.timers.Timer;

// TODO: This class doesn't do anything useful, please remove.
public class MetricCacheLoader {
    private MetricRegistry metricRegistry;

    public MetricCacheLoader(MetricRegistry registry) {
        this.metricRegistry = registry;
    }

    void mark(MetricId meterPath) {
        metricRegistry.meter(meterPath).inc();
    }

    Timer.Context startTimer(MetricId timerPath, List<? extends Number> metricBuckets) {
        return metricRegistry.timer(timerPath, metricBuckets).time();
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
