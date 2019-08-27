package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

public final class LaBanquePostaleConstants {
    public static final String INTEGRATION_NAME = "labanquepostale";

    public static class Urls {
        private static final String BASE_URL = "https://sandbox.labanquepostale.com";
        public static final String OAUTH = BASE_URL + "/authorize";
        public static final String GET_TOKEN = BASE_URL + "/token";
        public static final String FETCH_ACCOUNTS = BASE_URL + "/accounts";
        public static final String FETCH_BALANCES = BASE_URL + "/accounts/%s/balances";
        public static final String BASE_URL_WITH_SLASH = BASE_URL + "/";
        public static final String PAYMENT_INITIATION = BASE_URL + "/payment-requests";
        public static final String GET_PAYMENT = BASE_URL + "/payment-requests/%s";
        public static final String CONFIRM_PAYMENT = BASE_URL + "/payment-requests/%s/confirmation";
    }

    public static class StorageKeys {
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String SIGNATURE = "Signature";
        public static final String CONTENT_TYPE = "Content-Type";
    }

    public static class HeaderValues {

        public static final String CONTENT_TYPE = "application/json";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class Payload {
        public static final String EMPTY = "";
    }

    public static class PaymentTypeInformation {
        public static final String CATEGORY_PURPOSE = "DVPM";
        public static final String LOCAL_INSTRUMENT = "INST";
        public static final String SERVICE_LEVEL = "SEPA";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String PAYMENT_NOT_FOUND = "Payment can not be found";
    }

    public static class QueryValues {
        public static final String SCORE = "aisp";
    }
}
