package se.tink.backend.aggregation.eidasidentity;

import com.google.inject.Inject;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.libraries.unleash.UnleashClient;

public class CertificateIdProviderImpl implements CertificateIdProvider {
    private static final String TINK_ORGANIZATION_ID = "PSDSE-FINA-44059";
    private final TppSecretsServiceClient tppSecretsServiceClient;
    private final EidasMigrationToggle eidasMigrationToggle;

    @Inject
    public CertificateIdProviderImpl(
            ManagedTppSecretsServiceClient tppSecretsServiceClient, UnleashClient unleashClient) {
        this.tppSecretsServiceClient = tppSecretsServiceClient;
        this.eidasMigrationToggle = new EidasMigrationToggle(unleashClient);
    }

    @Override
    public String getCertId(
            String appId, String clusterId, String providerName, String marketCode) {
        if (Stream.of("UK", "GB").anyMatch(market -> market.equalsIgnoreCase(marketCode))) {
            return AvailableCertIds.UKOB.getValue();
        } else if (isUnderTinkLicence(appId, clusterId, providerName)) {
            return eidasMigrationToggle.getEnabledCertId(marketCode, providerName).getValue();
        } else {
            return AvailableCertIds.DEFAULT.getValue();
        }
    }

    private boolean isUnderTinkLicence(String appId, String clusterId, String providerName) {
        if (tppSecretsServiceClient.isEnabled()) {
            return tppSecretsServiceClient
                    .getAllSecrets(
                            appId, clusterId, AvailableCertIds.DEFAULT.getValue(), providerName)
                    .map(SecretsEntityCore::getQwac)
                    .filter(this::isTinkCertificate)
                    .isPresent();
        }
        return true;
    }

    @SneakyThrows
    private boolean isTinkCertificate(String qwac) {
        return CertificateUtils.getOrganizationIdentifier(qwac).equals(TINK_ORGANIZATION_ID);
    }
}
