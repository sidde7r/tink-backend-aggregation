package se.tink.backend.integration.tpp_secrets_service.client.iface;

import java.util.Optional;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;

public interface TppSecretsServiceClient {
    Optional<SecretsEntityCore> getAllSecrets(
            String appId, String clusterId, String certId, String providerId);

    void ping();

    boolean isEnabled();
}
