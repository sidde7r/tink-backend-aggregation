package se.tink.backend.aggregation.startupchecks;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.util.Collection;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class StartupChecksHandlerImpl implements StartupChecksHandler {

    private static final Logger logger = LoggerFactory.getLogger(StartupChecksHandlerImpl.class);

    private final Collection<HealthCheck> healthChecks;

    private final HealthCheckMetricsAggregation healthCheckMetricsAggregation;

    @Inject
    public StartupChecksHandlerImpl(
            MetricRegistry metricRegistry,
            SecretsServiceHealthCheck secretsServiceHealthCheck,
            EidasProxySignerHealthCheck eidasProxySignerHealthCheck) {
        this.healthCheckMetricsAggregation = new HealthCheckMetricsAggregation(metricRegistry);
        healthChecks =
                ImmutableSet.<HealthCheck>builder()
                        .add(secretsServiceHealthCheck)
                        .add(eidasProxySignerHealthCheck)
                        .build();
    }

    @Override
    public String handle() {
        try {
            isHealthy();
        } catch (NotHealthyException e) {
            logger.error("Health checks failed.", e);
            HttpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE);
        }
        return "started";
    }

    private void isHealthy() throws NotHealthyException {
        for (HealthCheck healthCheck : healthChecks) {
            healthCheckMetricsAggregation.checkAndObserve(healthCheck);
        }
    }
}
