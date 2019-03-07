package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb;

public abstract class SEBConstants {

    public static class Urls {
        public static final String BASE_AUTH = "https://sandbox.sebgroup.com/authentication/v2";
        public static final String OAUTH = "/oauth2/authorize";
        public static final String BASE_AIS = "https://sandbox.sebgroup.com/ais/v3";
        public static final String ACCOUNTS = "/accounts";
        public static final String TRANSACTIONS = "/accounts/{accountId}/transactions";
        public static final String TOKEN = "/oauth2/token";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String GRANT_TYPE = "grant_type";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String TRANSACTION_SEQUENCE_NUMBER = "transactionSequenceNumber";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE_TOKEN = "code";
        public static final String SCOPE = "psd2_accounts";
        public static final String GRAND_TYPE = "authorization_code";
        public static final String BOOKED_TRANSACTIONS = "booked";
        public static final String WITH_BALANCE = "true";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
    }

    public static class STORAGE {
        public static final String TOKEN = "TOKEN";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String REDIRECT_URI = "REDIRECT_URI";
    }

    public static class ACCOUNTS {
        public static final String AVAILABLE_BALANCE = "interimAvailable";
        public static final String STATUS_ENABLED = "enabled";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class FETCHER {
        public static final int START_PAGE = 1;
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "seb";
        public static final String CLIENT_NAME = "tink";
    }
}
