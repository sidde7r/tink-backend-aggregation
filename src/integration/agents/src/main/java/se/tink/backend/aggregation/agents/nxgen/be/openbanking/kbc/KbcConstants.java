package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

public abstract class KbcConstants {

    public static final String INTEGRATION_NAME = "kbc";

    private KbcConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String AIS_PRODUCT = "/psd2";
        private static final String BASE_AUTH = "/ASK/oauth";
        public static final String AUTH = BASE_AUTH + "/authorize/1";
        public static final String TOKEN = BASE_AUTH + "/token/1";
        private static final String BASE_AIS = AIS_PRODUCT + "/v2";
        public static final String CONSENT = BASE_AIS + "/consents";
        public static final String ACCOUNTS = BASE_AIS + "/accounts";
        public static final String BASE_PIS = "/psd2/v2/";
        public static final String PAYMENTS = BASE_PIS + "/payments/{paymentProduct}";
        public static final String PAYMENT_STATUS = PAYMENTS + "/{paymentId}/status";
        public static final String AUTHORIZE_PAYMENT =
                BASE_PIS + "/authorization/payments/{paymentId}";
    }
}
