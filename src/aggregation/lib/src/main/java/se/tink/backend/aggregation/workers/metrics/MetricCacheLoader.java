package se.tink.backend.aggregation.workers.metrics;

import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

// TODO: This class doesn't do anything useful, please remove.
public class MetricCacheLoader {
    private MetricRegistry metricRegistry;

    public MetricCacheLoader(MetricRegistry registry) {
        this.metricRegistry = registry;
    }

    void mark(MetricId meterPath) {
        metricRegistry.meter(meterPath).inc();
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
