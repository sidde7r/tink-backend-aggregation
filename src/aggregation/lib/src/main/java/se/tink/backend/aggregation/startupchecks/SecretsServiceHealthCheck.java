package se.tink.backend.aggregation.startupchecks;

import java.util.concurrent.Callable;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;

public class SecretsServiceHealthCheck implements HealthCheck {

    private final TppSecretsServiceClient tppSecretsServiceClient;
    private final HealthCheckMetricsAggregation healthCheckMetricsAggregation;

    public SecretsServiceHealthCheck(
            ManagedTppSecretsServiceClient tppSecretsServiceClient,
            HealthCheckMetricsAggregation healthCheckMetricsAggregation) {
        this.tppSecretsServiceClient = tppSecretsServiceClient;
        this.healthCheckMetricsAggregation = healthCheckMetricsAggregation;
    }

    @Override
    public void check() throws NotHealthyException {
        healthCheckMetricsAggregation.checkAndObserve(this.getClass().getSimpleName(), rawCheck());
    }

    private Callable<Void> rawCheck() {
        return () -> {
            tppSecretsServiceClient.ping();
            return null;
        };
    }
}
