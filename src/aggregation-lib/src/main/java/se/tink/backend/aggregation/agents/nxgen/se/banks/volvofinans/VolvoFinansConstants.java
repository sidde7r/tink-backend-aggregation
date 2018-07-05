package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans;

import se.tink.backend.aggregation.nxgen.http.URL;

public class VolvoFinansConstants {

    public static final class Urls {
        public static final String ENTRY_POINT = "https://api.volvofinans.se";
        public static final URL LOGIN_BANKID_INIT = new URL(ENTRY_POINT + "/v1/identifiering/bankid");
        public static final URL LOGIN_BANKID_POLL = new URL(ENTRY_POINT + "/v1/identifiering/{identificationId}");
        public static final URL LOGOUT = new URL(ENTRY_POINT + "/v1/identifiering");
        public static final URL CUSTOMER = new URL(ENTRY_POINT + "/v1/kund");

        public static final URL CREDIT_CARD_ACCOUNTS = new URL(ENTRY_POINT + "/v2/kortkonton");
        public static final URL SAVINGS_ACCOUNTS = new URL(ENTRY_POINT + "/v1/sparkonton");

        public static final URL CREDIT_CARD_ACCOUNTS_TRANSACTIONS = new URL(ENTRY_POINT
                + "/v1/kortkonton/{accountId}/transaktioner");
        public static final URL SAVINGS_ACCOUNTS_TRANSACTIONS = new URL(ENTRY_POINT
                + "/v1/sparkonton/{accountId}/transaktioner");
    }

    public static final class UrlParameters {
        public static final String IDENTIFICATION_ID = "identificationId";
        public static final String ACCOUNT_ID = "accountId";
    }

    public static final class QueryParameters {
        public static final String FROM_DATE = "periodStart";
        public static final String TO_DATE = "periodSlut";
        public static final String LIMIT = "antal";
        public static final String OFFSET = "start";
    }

    public static final class Headers {
        // Request headers
        public static final String HEADER_HOST = "Host";
        public static final String VALUE_HOST = "api.volvofinans.se";
        public static final String HEADER_X_API_KEY = "X-API-KEY";
        public static final String VALUE_X_API_KEY = "Clgrvs8obSzvqsQZHy8fPn8GiwjFfMkt";
        public static final String HEADER_USER_AGENT = "User-Agent";
        public static final String VALUE_USER_AGENT = "vfb/3 CFNetwork/811.4.18 Darwin/16.5.0";
        public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
        public static final String VALUE_ACCEPT_ENCODING = "gzip, deflate";
        public static final String HEADER_CONNECTION = "Connection";
        public static final String VALUE_CONNECTION = "keep-alive";
        public static final String HEADER_BEARER_TOKEN = "Authorization";
        public static final String VALUE_BEARER_TOKEN_PREFIX = "Bearer ";

        // Response headers
        public static final String HEADER_LOCATION = "location";
    }

    public static final class Currencies {
        public static String SEK = "SEK";
    }

    public static final class BankIdStatus {
        public static final String DONE = "OK";
        public static final String WAITING = "PAGAENDE";
    }

    public static class Pagination {
        public static int LIMIT = 100;
    }

    public static class LogTags {
        public static final String SAVINGS_ACCOUNTS = "#se_VolvoFinans_savings_accounts";
        public static final String SAVINGS_ACCOUNT_TRANSACTIONS = "#se_VolvoFinans_savings_account_transactions";
    }
}

