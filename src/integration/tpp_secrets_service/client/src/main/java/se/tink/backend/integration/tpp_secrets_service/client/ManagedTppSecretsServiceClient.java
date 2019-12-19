package se.tink.backend.integration.tpp_secrets_service.client;

import io.dropwizard.lifecycle.Managed;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;

public interface ManagedTppSecretsServiceClient extends Managed, TppSecretsServiceClient {}
