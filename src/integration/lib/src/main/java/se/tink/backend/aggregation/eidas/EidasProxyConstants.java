package se.tink.backend.aggregation.eidas;

public final class EidasProxyConstants {

    public static class Url {
        public static final String EIDAS_SIGN = "sign-rsa-sha256/";
    }

    public static class Eidas {
        public static final String CLIENT_P12 = "data/eidas_dev_certificates/eidas_client.p12";
        public static final String CLIENT_PASSWORD = "changeme";
        public static final String DEV_CAS_JKS = "data/eidas_dev_certificates/eidas_dev_ca.jks";
        public static final String DEV_CAS_PASSWORD = "changeme";
    }
}
