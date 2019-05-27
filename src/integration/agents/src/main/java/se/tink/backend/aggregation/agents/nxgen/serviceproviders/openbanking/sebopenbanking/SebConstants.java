package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking;

import java.time.ZoneId;

public abstract class SebConstants {

    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Stockholm");
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static class Urls {

        private static final String BASE_AUTH = "/mga/sps/oauth";
        private static final String BASE_AIS = "/ais/v5/identified2";
        private static final String BASE_CREDIT_CARD_ACCOUNTS = "/ais/v2/identified2";

        public static final String OAUTH = BASE_AUTH + "/oauth20/authorize";
        public static final String TOKEN = BASE_AUTH + "/oauth20/token";
        public static final String ACCOUNTS = BASE_AIS + "/accounts";
        public static final String TRANSACTIONS = BASE_AIS + "/accounts/{accountId}/transactions";
        public static final String CREDIT_CARD_ACCOUNTS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts/{accountId}/transactions";
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
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE_TOKEN = "code";
        public static final String SCOPE = "psd2_accounts psd2_payments";
        public static final String GRANT_TYPE = "authorization_code";
        public static final String BOOKED_TRANSACTIONS = "booked";
        public static final String WITH_BALANCE = "true";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
    }

    public static class StorageKeys {
        public static final String BASE_URL = "BASE_URL";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String REDIRECT_URI = "REDIRECT_URI";
        public static final String TOKEN = "TOKEN";
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
        public static final int START_PAGE = 0;
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "sebopenbanking";
        public static final String CLIENT_NAME = "tink";
    }
}
