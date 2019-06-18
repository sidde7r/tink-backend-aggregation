package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

public final class LaBanquePostaleConstants {
    public static final String INTEGRATION_NAME = "labanquepostale";

    public static class Urls {
        public static final String BASE_URL = "https://sandbox.labanquepostale.com";
        public static final String FETCH_ACCOUNTS = BASE_URL + "/accounts";
        public static final String FETCH_TRANSACTIONS = BASE_URL + "/accounts/%s/transactions";
        public static final String FETCH_BALANCES = BASE_URL + "/accounts/%s/balances";
        public static final String BASE_URL_WITH_SLASH = BASE_URL + "/";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String SIGNATURE = "Signature";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class QueryValues {
        public static final String QUERY = "?client_id=%s&client_secret=%s";
    }
}
