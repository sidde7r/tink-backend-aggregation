package se.tink.backend.aggregation.startupchecks;

import com.google.inject.Inject;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;

public class SecretsServiceHealthCheck implements HealthCheck {

    private final TppSecretsServiceClient tppSecretsServiceClient;

    @Inject
    public SecretsServiceHealthCheck(ManagedTppSecretsServiceClient tppSecretsServiceClient) {
        this.tppSecretsServiceClient = tppSecretsServiceClient;
    }

    @Override
    public void check() throws NotHealthyException {
        try {
            tppSecretsServiceClient.ping();
        } catch (Exception e) {
            throw new NotHealthyException(this.getClass().getSimpleName() + " failed.", e);
        }
    }
}
