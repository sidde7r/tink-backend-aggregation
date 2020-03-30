package se.tink.backend.aggregation.startupchecks;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.util.Collection;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class StartupChecksHandlerImpl implements StartupChecksHandler {

    private static final Logger logger = LoggerFactory.getLogger(StartupChecksHandlerImpl.class);

    // Used to fake a startup probe, we wait for this to be true one first time and then we do not
    // check again
    private boolean firstCheckPassed = false;

    private final Collection<HealthCheck> healthChecks;

    private final HealthCheckMetricsAggregation healthCheckMetricsAggregation;

    @Inject
    public StartupChecksHandlerImpl(
            ManagedTppSecretsServiceClient tppSecretsServiceClient, MetricRegistry metricRegistry) {
        this.healthCheckMetricsAggregation = new HealthCheckMetricsAggregation(metricRegistry);
        healthChecks = ImmutableSet.of(new SecretsServiceHealthCheck(tppSecretsServiceClient));
    }

    @Override
    public String handle() {
        if (!firstCheckPassed) {
            logger.info("Health checks haven't passed yet.");
            try {
                isHealthy();
            } catch (NotHealthyException e) {
                logger.error("Health checks failed.", e);
                HttpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE);
            }
            firstCheckPassed = true;
            logger.info("Health checks passed.");
        } else {
            try {
                isHealthy();
            } catch (NotHealthyException e) {
                logger.warn("Health checks failed.", e);
                return "experiencing problems";
            }
        }
        return "started";
    }

    private void isHealthy() throws NotHealthyException {
        for (HealthCheck healthCheck : healthChecks) {
            healthCheckMetricsAggregation.checkAndObserve(healthCheck);
        }
    }
}
