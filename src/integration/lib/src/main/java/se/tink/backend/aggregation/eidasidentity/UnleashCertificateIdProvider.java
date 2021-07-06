package se.tink.backend.aggregation.eidasidentity;

import com.google.inject.Inject;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.unleash.UnleashClient;

@Slf4j
public class UnleashCertificateIdProvider implements CertificateIdProvider {
    private final EidasMigrationToggle eidasMigrationToggle;

    @Inject
    public UnleashCertificateIdProvider(UnleashClient unleashClient) {
        this.eidasMigrationToggle = new EidasMigrationToggle(unleashClient);
    }

    @Override
    public String getCertId(
            String appId,
            String clusterId,
            String providerName,
            String marketCode,
            boolean isOpenBanking) {
        AvailableCertIds certId = AvailableCertIds.DEFAULT;
        if (Stream.of("UK", "GB").anyMatch(market -> market.equalsIgnoreCase(marketCode))) {
            certId = AvailableCertIds.UKOB;
        } else if (isOpenBanking) {
            certId = eidasMigrationToggle.getEnabledCertId(appId, providerName);
        }
        log.info(
                "[CertIdProvider] Provide `{}` certId for [appId: `{}`, clusterId: `{}`, providerName: `{}`, marketCode: `{}`]",
                certId,
                appId,
                clusterId,
                providerName,
                marketCode);
        return certId.getValue();
    }
}
