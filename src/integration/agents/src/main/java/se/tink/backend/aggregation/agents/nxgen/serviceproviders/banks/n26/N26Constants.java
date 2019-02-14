package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class N26Constants {

    public static final String BASIC_AUTHENTICATION_TOKEN = "Basic aXBob25lOnNlY3JldA=="; // Deocded: iphone:secret
    public static final String BEARER_TOKEN = "Bearer ";
    public static final String AUTHENTICATION_ERROR = "invalid_grant";
    public static final String CURRENCY_EUR = "EUR";
    public static final int ONETHOUSAND = 1000;
    public static final String SPACE_ID = "spaceId";

    public static class URLS {
        public static final String HOST = "https://api.tech26.de";
        public static final String AUTHENTICATION =  "/oauth/token";
        public static final String ACCOUNT = "/api/accounts";
        public static final String TRANSACTION =  "/api/smrt/transactions";
        public static final String SAVINGS = "/api/hub/savings/accounts";
        public static final String FIXED_SAVINGS = "/api/hub/savings/fixedterms/accounts";
        public static final String SPACES_TRANSACTIONS = "/api/spaces/{spaceId}/transactions";
        public static final String SPACES_SAVINGS = "/api/spaces";
        public static final String LOGOUT = "/api/me/logout";
    }

    public static class Storage {
        public static final String TOKEN_ENTITY = "TOKEN_ENTITY";
    }

    public static class Queryparams {
        public static final String FULL = "full";
        public static final String LASTID = "lastId";
        public static final String LIMIT = "limit";
        public static final String TRANSACTION_LIMIT_DEFAULT = "20";
        public static final String SPACE_TRANSACTIONS_SIZE = "size";
        public static final String SPACE_LIMIT_DEFAULT = "20";
        public static final String SPACE_BEFOREID = "beforeId";
    }
    
    public static class Body {
        public static final String PASSWORD = "&password=";
        public static final String USERNAME = "&username=";
    }

    public static class Logging {
        public static final LogTag TRANSACTION_PAGINATION_ERROR = LogTag.from("N26_TRANSACTION_PAGINATION_ERROR");
    }
}
