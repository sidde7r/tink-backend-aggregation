package se.tink.backend.aggregation.eidasidentity;

public interface CertificateIdProvider {

    String getCertId(
            String appId,
            String clusterId,
            String providerName,
            String marketCode,
            boolean isOpenBanking);
}
