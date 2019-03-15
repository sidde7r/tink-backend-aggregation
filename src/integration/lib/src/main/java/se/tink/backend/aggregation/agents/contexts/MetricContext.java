package se.tink.backend.aggregation.agents.contexts;

import se.tink.libraries.metrics.MetricRegistry;

public interface MetricContext {
    MetricRegistry getMetricRegistry();
    void setMetricRegistry(MetricRegistry metricRegistry);

}
