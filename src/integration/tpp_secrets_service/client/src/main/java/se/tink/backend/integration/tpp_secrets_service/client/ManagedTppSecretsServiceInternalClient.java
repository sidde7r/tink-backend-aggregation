package se.tink.backend.integration.tpp_secrets_service.client;

import io.dropwizard.lifecycle.Managed;
import se.tink.backend.secretsservice.client.SecretsServiceInternalClient;

public interface ManagedTppSecretsServiceInternalClient
        extends Managed, SecretsServiceInternalClient {}
