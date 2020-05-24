package se.tink.backend.aggregation.workers.metrics;

import se.tink.libraries.metrics.registry.MetricRegistry;

// TODO: This class doesn't do anything useful, please remove.
public class MetricCacheLoader {
    private MetricRegistry metricRegistry;

    public MetricCacheLoader(MetricRegistry registry) {
        this.metricRegistry = registry;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
