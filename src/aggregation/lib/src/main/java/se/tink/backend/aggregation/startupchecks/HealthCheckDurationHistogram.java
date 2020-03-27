package se.tink.backend.aggregation.startupchecks;

import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class HealthCheckDurationHistogram {

    private static final MetricId HEALTHCHECK_DURATION_HISTOGRAM =
            MetricId.newId("healthcheck_duration_seconds");
    private final MetricRegistry metricRegistry;

    public HealthCheckDurationHistogram(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void update(String name, boolean healthy, double duration) {
        metricRegistry
                .histogram(
                        HEALTHCHECK_DURATION_HISTOGRAM
                                .label("name", name)
                                .label("healthy", healthy))
                .update(duration);
    }
}
