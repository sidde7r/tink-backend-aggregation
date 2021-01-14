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
        public static final String TRANSACTIONS = ACCOUNTS + "/{accountId}/transactions";
        public static final String CARD_ACCOUNTS = AIS_PRODUCT + "/card-accounts";
        public static final String CARD_TRANSACTIONS = CARD_ACCOUNTS + "/{accountId}/transactions";
    }

    public static class HeaderKeys {
        private HeaderKeys() {}

        public static final String DIGEST = "Digest";
        public static final String SIGNATURE = "Signature";
        public static final String API_KEY = "apikey";
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String ENTRY_REFERENCE_FROM = "entryReferenceFrom";
        public static final String DATE_FROM = "dateFrom";
    }

    public static final class BookingStatusParameter {
        private BookingStatusParameter() {}

        public static final String BOOKED = "booked";
        public static final String PENDING = "pending";
        public static final String BOTH = "both";
    }

    public static class PathVariables {
        private PathVariables() {}

        public static final String ACCOUNT_ID = "accountId";
    }
}
