package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

public final class LaBanquePostaleConstants {
    public static final String INTEGRATION_NAME = "labanquepostale";

    public static class Urls {
        public static final String BASE_URL = "https://sandbox.labanquepostale.com";
        public static final String FETCH_ACCOUNTS = BASE_URL + "/accounts";
        public static final String FETCH_TRANSACTIONS = BASE_URL + "/accounts/%s/transactions";
        public static final String FETCH_BALANCES = BASE_URL + "/accounts/%s/balances";
        public static final String BASE_URL_WITH_SLASH = BASE_URL + "/";
        public static final String PAYMENT_INITIATION = Urls.BASE_URL + "/payment-requests";
        public static final String GET_PAYMENT = Urls.BASE_URL + "/payment-requests/%s";
        public static final String CONFIRM_PAYMENT =
                Urls.BASE_URL + "/payment-requests/%s/confirmation";
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
        public static final String PAYMENT_NOT_FOUND = "Payment can not be found";
    }
}
