package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.rabobank;

public final class RabobankConstants {

    public static class Url {
        public static final String EIDAS_SIGN =
                "https://eidas-proxy.staging.aggregation.tink.network/sign-rsa-sha256/";
    }

    public static class Eidas {
        public static final String CLIENT_P12 = "data/eidas_dev_certificates/eidas_client.p12";
        public static final String CLIENT_PASSWORD = "changeme";
        public static final String DEV_CAS_JKS = "data/eidas_dev_certificates/eidas_dev_ca.jks";
        public static final String DEV_CAS_PASSWORD = "changeme";
    }
}
