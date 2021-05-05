package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

public final class BawagPskConstants {

    private BawagPskConstants() {
        throw new AssertionError();
    }

    public static class Header {
        public static final String ACCEPT = "*/*";
        public static final String USER_AGENT =
                "Tink (+https://www.tink.se/; noc@tink.se) mobileBanking/bawag_5.3 target/PROD";
        public static final String ACCEPT_LANGUAGE = "en-gb";
    }

    public static class Urls {
        public static final String SERVICE_ENDPOINT = "/ebanking.mobile/SelfServiceMobileService";
    }

    public static class Tls {
        public static final String INTERMEDIATE_CERT_PATH =
                "data/agents/at/bawagpsk/DigiCert_SHA2_Extended_Validation_Server_CA.jks";
        public static final String INTERMEDIATE_CERT_PASSWORD = "tinktink";
    }

    public enum Storage {
        SERVER_SESSION_ID,
        QID,
        PRODUCTS,
        PRODUCT_CODES
    }
}
