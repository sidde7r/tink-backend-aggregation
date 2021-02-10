package se.tink.backend.aggregation.agents.nxgen.se.other.csn;

public class CSNConstants {

    public static final String CURRENCY = "SEK";

    public static class Urls {
        private static final String BASE_URL = "https://www.csn.se";
        private static final String BASE_URL_LOGIN = "https://tjanster.csn.se/bas";

        static final String LOGIN_BANKID = BASE_URL_LOGIN + "/inloggning/mobilbid.do";
        static final String BANKID_POLL = BASE_URL_LOGIN + "/BankID";

        static final String CURRENT_DEBT = BASE_URL + "/api/kund-api-webb/rest/kund/aktuellskuld";
        static final String USER_INFO = BASE_URL + "/api/kund-api-webb/rest/kund/info";
    }

    public static class BankIdStatus {
        public static final String COLLECT = "collect";
        public static final String RECEIVED = "received";
        public static final String CONTINUE = "continue";
    }

    public static class LoginKeys {
        public static final String METHOD = "metod";
        public static final String SSN = "pnr";
        public static final String CSN_LOGIN = "csn_login";
    }

    public static class LoginValues {
        public static final String VALIDATE_BANK_ID = "valideramobilbid";
        public static final String TRY_LOGIN = "tryLogin";
        public static final String BANK_ID = "mobilbid";
    }

    public static class Storage {
        public static final String SESSION_ID = "JSESSIONID";
        public static final String ACCESS_TOKEN = "access_token";
    }

    public static class LoanTypes {
        public static final String ANNUTITY_LOAN = "ÅBAL";
        public static final String STUDENT_LOAN = "ÅBSL";
        public static final String STUDENT_AID = "ÅBSM";
    }

    static class HeaderKeys {
        static final String REFERER = "Referer";
        static final String CSN_AUTHORIZATION = "CSN_Authorization";
    }

    static class HeaderValues {
        static final String BEARER = "Bearer ";
        static final String USER_AGENT = "Mozilla/5.0";
    }
}
