package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

public abstract class DeutscheBankConstants {

    public static class Urls {
        private static final String BASE_AUTH = "/gw/oidc";
        public static final String OAUTH = BASE_AUTH + "/authorize";
        public static final String TOKEN = BASE_AUTH + "/token";
        private static final String BASE_AIS = "/gw/dbapi/banking";
        public static final String ACCOUNTS = BASE_AIS + "/cashAccounts/v2";
        public static final String TRANSACTIONS = BASE_AIS + "/transactions/v2";
        private static final String BASE_REFERENCE_DATA = "/gw/dbapi/referenceData";
        public static final String PARTNERS = BASE_REFERENCE_DATA + "/partners/v2";
    }

    public static class StorageKeys {
        public static final String BASE_URL = "BASE_URL";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String REDIRECT_URI = "REDIRECT_URI";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String TOKEN = "TOKEN";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String IBAN = "iban";
        public static final String OFFSET = "offset";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "code";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String GRANT_TYPE = "authorization_code";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL =
                "Invalid Config: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public class Market {
        public static final String INTEGRATION_NAME = "deutschebank";
        public static final String CLIENT_NAME = "tink";
    }

    public class Accounts {
        public static final String PARTNER_TYPE_NATURAL = "NATURAL_PERSON";
        public static final String CURRENT_ACCOUNT = "CURRENT_ACCOUNT";
    }

    public class Fetcher {
        public static final int START_PAGE = 1;
    }
}
