package se.tink.backend.aggregation.agents.contexts;

import se.tink.libraries.metrics.registry.MetricRegistry;

public interface MetricContext {
    MetricRegistry getMetricRegistry();

    void setMetricRegistry(MetricRegistry metricRegistry);
}
