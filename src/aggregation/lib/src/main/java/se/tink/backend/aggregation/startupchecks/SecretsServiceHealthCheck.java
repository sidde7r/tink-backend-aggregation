package se.tink.backend.aggregation.startupchecks;

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;

public class SecretsServiceHealthCheck implements HealthCheck {

    private final TppSecretsServiceClient tppSecretsServiceClient;
    private final HealthCheckDurationHistogram healthCheckDurationHistogram;

    public SecretsServiceHealthCheck(
            ManagedTppSecretsServiceClient tppSecretsServiceClient,
            HealthCheckDurationHistogram healthCheckDurationHistogram) {
        this.tppSecretsServiceClient = tppSecretsServiceClient;
        this.healthCheckDurationHistogram = healthCheckDurationHistogram;
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
            healthCheckDurationHistogram.update(
                    this.getClass().getSimpleName(),
                    healthy,
                    stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0);
        }
    }
}
