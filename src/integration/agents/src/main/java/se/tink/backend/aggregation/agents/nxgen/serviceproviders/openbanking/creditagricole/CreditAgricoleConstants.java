package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

public abstract class CreditAgricoleConstants {

    public static class Urls {
        public static final String AUTHENTICATION = "/castore-data-provider/authentification/";
        public static final String GET_REQUEST_TOKEN =
                "/castore-oauth/resources/1/oauth/get_request_token";
        public static final String GET_ACCESS_TOKEN =
                "/castore-oauth/resources/1/oauth/get_access_token";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String OAUTH_TOKEN = "oauth_token";
        public static final String TINK_STATE = "tink_state";
    }

    public static class FormKeys {
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
        public static final String OAUTH_CALLBACK_CONFIRMED = "OAUTH_CALLBACK_CONFIRMED";
        public static final String OAUTH_TOKEN_SECRET = "OAUTH_TOKEN_SECRET";
    }

    public static class StorageKeys {
        public static final String BASE_URL = "BASE_URL";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String TEMPORARY_TOKEN = "TEMPORARY_TOKEN";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "creditagricoleoauth";
        public static final String CLIENT_NAME = "tink";
    }
}
