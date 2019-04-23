package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard;

public class EnterCardConstants {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String RESPONSE_FORM_ID = "responseForm";
    static final String REFERER = "Referer";
    static final String SIGNICAT_ID = "sbid-mobil-2014:%s:sv";

    public static class Urls {

        static final String ORDER = "/order";
        static final String BANK_ID_HOST = "https://id.signicat.com";
        public static final String BANK_ID_INIT = BANK_ID_HOST + "/std/method/entercard";

        static final String USER_ENDPOINT = "/darwin/api/user";
        static final String ACCOUNT_ENDPOINT = "/darwin/api/card-account/";

        // No slash here, the card-account API requires a trailing slash so we already have it
        static final String TRANSACTIONS_ENDPOINT = "transactions";
    }

    public static class BankIdProgressStatus {

        public static final String COMPLETE = "COMPLETE";
        public static final String OUTSTANDING_TRANSACTION = "OUTSTANDING_TRANSACTION";
        public static final String USER_SIGN = "USER_SIGN";
        public static final String NO_CLIENT = "NO_CLIENT";
    }

    static class QueryKey {

        static final String PREFILLED_MODE = "prefilled.mode";
        static final String PAGE = "page";
        static final String PER_PAGE = "perPage";
        static final String ID = "id";
        static final String TARGET = "target";
    }

    static class QueryValue {

        static final String PREFILLED_MODE = "limited";
    }

    static class HeaderValue {

        static final String ACCEPT_LANGUAGE = "sv-se";
    }
}
