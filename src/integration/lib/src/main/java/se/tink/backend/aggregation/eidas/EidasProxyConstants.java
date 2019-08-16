package se.tink.backend.aggregation.eidas;

public final class EidasProxyConstants {

    public enum Algorithm {
        EIDAS_RSA_SHA256("sign-rsa-sha256/"),
        EIDAS_RSA_PSS256("sign-rsa-pss256/"),
        EIDAS_JWS_EC256("sign-jws-ec256/"),
        EIDAS_JWS_PS256("sign-jws-ps256/");

        private final String signingType;

        Algorithm(String signingType) {
            this.signingType = signingType;
        }

        public String getSigningType() {
            return this.signingType;
        }
    }

    public static class Eidas {
        public static final String CLIENT_P12 = "data/eidas_dev_certificates/eidas_client.p12";
        public static final String CLIENT_PASSWORD = "changeme";
        public static final String DEV_CAS_JKS = "data/eidas_dev_certificates/eidas_dev_ca.jks";
        public static final String DEV_CAS_PASSWORD = "changeme";
    }

    public enum CertificateId {
        ABN_AMRO("abnamro-QSEALC"),
        TINK("Tink-qsealc");

        private final String id;

        CertificateId(final String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
