package se.tink.backend.aggregation.startupchecks;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

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

        // Kubernetes will send a SIGTERM when the pod is being shutdown due to, e.g., a deploy.
        // When this happens we must immediately fail the readinessProbe. See:
        // https://engineering-book.global-production.tink.network/engineering/software/reliability/health-lifecycle
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    isShuttingDown.set(true);
                                    logger.info("SIGTERM received");
                                }));
    }

    @Override
    public String handle() {

        if (isShuttingDown.get()) {
            HttpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE);
        }

        try {
            isHealthy();
        } catch (NotHealthyException e) {
            logger.error("Health checks failed.", e);
            HttpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE);
        }
        logger.info("Readiness probe is working");
        return "started";
    }

    private void isHealthy() throws NotHealthyException {
        for (HealthCheck healthCheck : healthChecks) {
            healthCheckMetricsAggregation.checkAndObserve(healthCheck);
        }
    }
}
