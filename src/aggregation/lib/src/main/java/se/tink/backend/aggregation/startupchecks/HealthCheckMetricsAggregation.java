package se.tink.backend.aggregation.startupchecks;

import com.google.common.base.Stopwatch;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
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

    public <E> E checkAndObserve(String name, Callable<E> healthCheckToObserve)
            throws NotHealthyException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            E result = healthCheckToObserve.call();
            metricRegistry.meter(HEALTH_CHECK_SUCCESSFUL_CHECKS_COUNTER).inc();
            return result;
        } catch (Exception e) {
            throw new NotHealthyException(name + " failed.", e);
        } finally {
            metricRegistry
                    .histogram(HEALTH_CHECK_DURATION_HISTOGRAM.label("name", name))
                    .update(stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0);
            metricRegistry.meter(HEALTH_CHECK_TOTAL_CHECKS_COUNTER).inc();
        }
    }
}
