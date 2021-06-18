package se.tink.backend.aggregation.eidasidentity;

import java.util.stream.Stream;

public class DefaultCertificateIdProvider implements CertificateIdProvider {

    @Override
    public String getCertId(
            String appId,
            String clusterId,
            String providerName,
            String marketCode,
            boolean isOpenBanking) {
        if (Stream.of("UK", "GB").anyMatch(market -> market.equalsIgnoreCase(marketCode))) {
            return AvailableCertIds.UKOB.getValue();
        }
        return AvailableCertIds.DEFAULT.getValue();
    }
}
