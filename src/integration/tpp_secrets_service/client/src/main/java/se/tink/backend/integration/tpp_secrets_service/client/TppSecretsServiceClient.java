package se.tink.backend.integration.tpp_secrets_service.client;

import io.dropwizard.lifecycle.Managed;
import java.util.Optional;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;

public interface TppSecretsServiceClient extends Managed {
    Optional<SecretsEntityCore> getAllSecrets(
            String financialInstitutionId, String appId, String clusterId);

    boolean isEnabled();
}
