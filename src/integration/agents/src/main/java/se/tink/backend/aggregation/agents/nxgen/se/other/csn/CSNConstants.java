package se.tink.backend.aggregation.agents.nxgen.se.other.csn;

public class CSNConstants {

    public static class Urls {
        private static final String BASE_URL = "https://www.csn.se";
        private static final String BASE_URL_LOGIN = "https://tjanster.csn.se/bas";

        static final String LOGIN_BANKID = BASE_URL_LOGIN + "/inloggning/mobilbid.do";
        static final String BANKID_POLL = BASE_URL_LOGIN + "/BankID";
    }

    public static class BankIdStatus {
        public static final String COLLECT = "collect";
        public static final String RECEIVED = "received";
        public static final String CONTINUE = "continue";
    }

    public static class Login {
        public static final String METHOD = "metod";
        public static final String VALIDATE_BANK_ID = "valideramobilbid";
        public static final String SSN = "pnr";
        public static final String TRY_LOGIN = "tryLogin";
        public static final String CSN_LOGIN = "csn_login";
        public static final String BANK_ID = "mobilbid";
    }

    public static class Storage {
        public static final String SESSION_ID = "JSESSIONID";
        public static final String ACCESS_TOKEN = "access_token";
    }

    static class HeaderKeys {
        static final String USER_AGENT = "User-Agent";
        static final String REFERER = "Referer";
    }

    static class HeaderValues {
        static final String USER_AGENT = "Mozilla/5.0";
    }
}
