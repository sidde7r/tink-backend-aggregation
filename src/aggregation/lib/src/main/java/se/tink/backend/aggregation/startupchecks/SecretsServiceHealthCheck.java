package se.tink.backend.aggregation.startupchecks;

import com.google.common.base.Stopwatch;
import io.prometheus.client.Histogram;
import java.util.concurrent.TimeUnit;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;

public class SecretsServiceHealthCheck implements HealthCheck {

    private final Histogram healthCheckDuration;

    private final TppSecretsServiceClient tppSecretsServiceClient;

    public SecretsServiceHealthCheck(
            ManagedTppSecretsServiceClient tppSecretsServiceClient, Histogram healthCheckDuration) {
        this.tppSecretsServiceClient = tppSecretsServiceClient;
        this.healthCheckDuration = healthCheckDuration;
    }

    @Override
    public void check() throws NotHealthyException {
        boolean healthy = false;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            tppSecretsServiceClient.ping();
            healthy = true;
        } catch (Exception e) {
            throw new NotHealthyException(e);
        } finally {
            healthCheckDuration
                    .labels(this.getClass().getSimpleName(), String.valueOf(healthy))
                    .observe(stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0);
        }
    }
}
