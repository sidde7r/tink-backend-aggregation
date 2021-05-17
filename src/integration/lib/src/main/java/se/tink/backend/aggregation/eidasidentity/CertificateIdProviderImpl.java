package se.tink.backend.aggregation.eidasidentity;

import com.google.inject.Inject;
import java.security.cert.CertificateException;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.libraries.unleash.UnleashClient;

@Slf4j
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
            AvailableCertIds certId =
                    eidasMigrationToggle.getEnabledCertId(marketCode, providerName);
            return certId.getValue();
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

    private boolean isTinkCertificate(String qwac) {
        try {
            return CertificateUtils.getOrganizationIdentifier(qwac).equals(TINK_ORGANIZATION_ID);
        } catch (CertificateException | IllegalStateException e) {
            log.warn("Couldn't extract organization_id from QWAC certificate.", e);
            return false;
        }
    }
}
