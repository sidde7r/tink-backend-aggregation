package se.tink.backend.integration.tpp_secrets_service.client.iface;

import java.util.Optional;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;

public interface TppSecretsServiceClient {
    Optional<SecretsEntityCore> getAllSecrets(
            String appId, String clusterId, String certId, String providerId);

    Optional<String> getLicenseModel(String appId, String clusterId, String providerId);

    void ping();

    // control if secrets service new client should be used
    // will be removed after the migration
    boolean isUseSecretsServiceInternalClient();

    // control the percentage of live traffic using new client
    // will be removed after the migration
    String getRate();
}
