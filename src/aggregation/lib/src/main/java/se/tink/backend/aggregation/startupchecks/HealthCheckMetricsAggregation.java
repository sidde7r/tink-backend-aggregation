package se.tink.backend.aggregation.startupchecks;

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class HealthCheckMetricsAggregation {

    private static final MetricId HEALTH_CHECK_DURATION_HISTOGRAM =
            MetricId.newId("aggregation_health_check_duration_seconds");
    private static final MetricId HEALTH_CHECK_SUCCESSFUL_CHECKS_COUNTER =
            MetricId.newId("aggregation_successful_health_checks");
    private static final MetricId HEALTH_CHECK_TOTAL_CHECKS_COUNTER =
            MetricId.newId("aggregation_total_health_checks");
    private final MetricRegistry metricRegistry;

    public HealthCheckMetricsAggregation(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void checkAndObserve(HealthCheck healthCheck) throws NotHealthyException {
        String name = healthCheck.getClass().getSimpleName();
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            healthCheck.check();
            metricRegistry.meter(HEALTH_CHECK_SUCCESSFUL_CHECKS_COUNTER.label("name", name)).inc();
        } catch (Exception e) {
            throw new NotHealthyException(name + " failed.", e);
        } finally {
            metricRegistry
                    .histogram(HEALTH_CHECK_DURATION_HISTOGRAM.label("name", name))
                    .update(stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0);
            metricRegistry.meter(HEALTH_CHECK_TOTAL_CHECKS_COUNTER.label("name", name)).inc();
        }
    }
}
