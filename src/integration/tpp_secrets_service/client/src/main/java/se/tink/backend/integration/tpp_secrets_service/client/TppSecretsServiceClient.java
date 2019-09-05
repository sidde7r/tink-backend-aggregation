package se.tink.backend.integration.tpp_secrets_service.client;

import io.dropwizard.lifecycle.Managed;
import java.util.Map;
import java.util.Optional;

public interface TppSecretsServiceClient extends Managed {
    Optional<Map<String, String>> getAllSecrets(
            String financialInstitutionId, String appId, String clusterId);

    boolean isEnabled();
}
