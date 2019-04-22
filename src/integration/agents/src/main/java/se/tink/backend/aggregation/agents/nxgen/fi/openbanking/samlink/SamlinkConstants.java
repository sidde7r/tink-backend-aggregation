package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink;

public final class SamlinkConstants {

    public static final String INTEGRATION_NAME = "samlink";

    private SamlinkConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String TOKEN = "/samlink-api-sandbox/oauth/token";
        public static final String AUTH = "/samlink-api-sandbox/oauth/authorize";

        public static final String AIS_PRODUCT = "/psd2/v1";
        public static final String CONSENT = AIS_PRODUCT + "/consents";
        public static final String ACCOUNTS = AIS_PRODUCT + "/accounts";
        public static final String TRANSACTIONS = AIS_PRODUCT + "/accounts/%s/transactions";
    }

    public static class HeaderKeys {
        public static final String SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    }
}
