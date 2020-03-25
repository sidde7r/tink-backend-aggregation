package se.tink.backend.integration.tpp_secrets_service.client.iface;

import java.util.Optional;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;

public interface TppSecretsServiceClient {
    Optional<SecretsEntityCore> getAllSecrets(
            String financialInstitutionId, String appId, String clusterId);

    void ping();

    boolean isEnabled();
}
