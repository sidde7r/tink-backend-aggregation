package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension;

public class MinPensionConstants {
    public static final String CURRENCY_SEK = "SEK";

    static class Urls {
        private static final String HOST = "https://api.minpension.se";
        private static final String AUTH_HOST = "https://login-api.minpension.se";

        static final String INIT_BANKID = AUTH_HOST + Endpoints.INIT_BANKID;
        static final String POLL_BANKID = AUTH_HOST + Endpoints.POLL_BANKID;
        static final String FETCH_USER = HOST + Endpoints.FETCH_USER;
        static final String FETCH_SSN = HOST + Endpoints.FETCH_SSN;
        static final String FETCH_PENSION_ACCOUNTS = HOST + Endpoints.FETCH_PENSION_ACCOUNTS;
    }

    static class Endpoints {
        static final String INIT_BANKID = "/bankid/authenticate";
        static final String POLL_BANKID = "/bankid/get-status";
        static final String FETCH_USER = "/account";
        static final String FETCH_SSN = "/authentication";
        static final String FETCH_PENSION_ACCOUNTS = "/pensionstillgangar";
    }

    static class HeaderKeys {
        static final String USER_AGENT = "User-Agent";
    }

    static class HeaderValues {
        static final String USER_AGENT = "Mozilla/5.0";
    }

    public static class BankIdStatus {
        public static final String WAITING = "AWAITING";
        public static final String COMPLETE = "COMPLETE";
        public static final String CANCELLED = "CANCELED";
    }

    public static class StorageKeys {
        public static final String FIRST_NAME = "FIRST_NAME";
        public static final String LAST_NAME = "LAST_NAME";
    }

    public static class ErrorMessage {
        public static final String KNOW_YOUR_CUSTOMER =
                "To continue using this app you must answer some questions from your bank. Please log in with your bank's app or website.";
    }

    public static class AccountTypes {
        public static final String GENERAL_PENSION = "GENERALPENSION";
        public static final String INCOME_PENSION = "INCOME_PENSION";
        public static final String PREMIUM_PENSION = "PREMIUM_PENSION";
    }
}
