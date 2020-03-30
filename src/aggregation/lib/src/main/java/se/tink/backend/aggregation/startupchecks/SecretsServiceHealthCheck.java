package se.tink.backend.aggregation.startupchecks;

import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;

public class SecretsServiceHealthCheck implements HealthCheck {

    private final TppSecretsServiceClient tppSecretsServiceClient;

    public SecretsServiceHealthCheck(ManagedTppSecretsServiceClient tppSecretsServiceClient) {
        this.tppSecretsServiceClient = tppSecretsServiceClient;
    }

    @Override
    public void check() {
        tppSecretsServiceClient.ping();
    }
}
