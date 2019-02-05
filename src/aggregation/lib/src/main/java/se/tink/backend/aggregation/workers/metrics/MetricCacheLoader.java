package se.tink.backend.aggregation.workers.metrics;

import java.util.List;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

// TODO: This class doesn't do anything useful, please remove.
public class MetricCacheLoader {
    private static final AggregationLogger log = new AggregationLogger(MetricCacheLoader.class);

    private MetricRegistry metricRegistry;

    public MetricCacheLoader(MetricRegistry registry) {
        this.metricRegistry = registry;
    }

    void mark(MetricId meterPath) {
        metricRegistry.meter(meterPath).inc();
    }

    Timer.Context startTimer(MetricId timerPath) {
        return metricRegistry.timer(timerPath).time();
    }

    Timer.Context startTimer(MetricId timerPath, List<? extends Number> metricBuckets) {
        return metricRegistry.timer(timerPath, metricBuckets).time();
    }
}
