package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink;

public final class SamlinkConstants {

    private SamlinkConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        private Urls() {}

        public static final String TOKEN = "/oauthproxy/token";
        public static final String AUTH = "/oauthproxy/authorize";

        public static final String AIS_PRODUCT = "/psd2/v1";
        public static final String CONSENT = AIS_PRODUCT + "/consents";
        public static final String ACCOUNTS = AIS_PRODUCT + "/accounts";
        public static final String TRANSACTIONS = ACCOUNTS + "/%s/transactions";
    }

    public static class HeaderKeys {
        private HeaderKeys() {}

        public static final String DIGEST = "Digest";
        public static final String SIGNATURE = "Signature";
        public static final String API_KEY = "apikey";
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    }

    public static final class BookingStatus {
        private BookingStatus() {}

        public static final String BOOKED = "booked";
        public static final String PENDING = "pending";
    }
}
