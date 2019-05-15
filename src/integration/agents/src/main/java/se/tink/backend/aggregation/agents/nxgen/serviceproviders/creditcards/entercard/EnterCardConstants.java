package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard;

public class EnterCardConstants {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
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

    static class AuthenticationErrors {

        static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";
        static final String INVALID_STATUS = "INVALID_STATUS";
        static final String LOGIN_CANCELLED_BY_USER = "LOGIN_CANCELLED_BY_USER";
        static final String TECHNICAL_ERROR = "TECHNICAL_ERROR";
        static final String ERROR_IN_SIGNICAT_RESPONSE = "ERROR_IN_SIGNICAT_RESPONSE";
        static final String REAUTH_FAILURE = "REAUTH_FAILURE";
    }

    public static class AuthenticationForm {

        public static final String RESPONSE_FORM_ID = "responseForm";
        static final String ATTRIBUTE_KEY = "action";
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

    static class HeaderKey {

        static final String REFERER = "Referer";
        static final String ORIGIN = "Origin";
    }

    static class HeaderValue {

        static final String ACCEPT_LANGUAGE = "sv-se";
        static final String ORIGIN = "https://id.signicat.com";
    }

    public static class TransactionType {

        public static final String AUTHORIZATION = "Authorization";
    }
}
