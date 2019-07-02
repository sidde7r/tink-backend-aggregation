package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb;

public abstract class SebConstants {

    public static final String INTEGRATION_NAME = "seb";

    public static class Urls {
        public static final String BRANDED_ACCOUNTS = "/ais/v1/identified2/branded-card-accounts";
        public static final String BRANDED_TRANSACTIONS = "/{accountId}/transactions";
        public static final String BASE_URL = "https://api-sandbox.sebgroup.com";
        public static final String OAUTH = BASE_URL + "/mga/sps/oauth/oauth20/authorize";
        public static final String TOKEN = BASE_URL + "/mga/sps/oauth/oauth20/token";
        private static final String BASE_AIS = "/ais/v5";
        public static final String ACCOUNTS = BASE_URL + BASE_AIS + "/identified2/accounts";
        public static final String TRANSACTIONS =
                BASE_URL + BASE_AIS + "/identified2/accounts/{accountId}/transactions";
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
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BRAND_ID = "brandId";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE_TOKEN = "code";
        public static final String SCOPE = "psd2_accounts psd2_payments";
        public static final String GRANT_TYPE = "authorization_code";
        public static final String BOOKED_TRANSACTIONS = "booked";
        public static final String WITH_BALANCE = "true";
        public static final String EUROCARD_BRAND_ID = "ecse";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String PSU_CORPORATE_ID = "PSU-Corporate-ID";
    }

    public static class StorageKeys {
        public static final String BASE_URL = "BASE_BRANDED_URL";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String REDIRECT_URI = "REDIRECT_URI";
        public static final String TOKEN = "OAUTH_TOKEN";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class Accounts {
        public static final String AVAILABLE_BALANCE = "interimAvailable";
        public static final String STATUS_ENABLED = "enabled";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class Fetcher {
        public static final int START_PAGE = 1;
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class Format {
        public static final String TIMEZONE = "UTC";
        public static final String TIMESTAMP = "yyyy-MM-dd";
    }

    public static class HeaderValues {

        public static final Object PSU_CORPORATE_ID = "40073144970009";
    }
}
