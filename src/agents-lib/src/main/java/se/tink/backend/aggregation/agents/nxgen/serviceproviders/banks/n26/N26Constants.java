package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class N26Constants {

    public static final String BASIC_AUTHENTICATION_TOKEN = "Basic aXBob25lOnNlY3JldA=="; // Deocded: iphone:secret
    public static final String BEARER_TOKEN = "Bearer ";
    public static final String AUTHENTICATION_ERROR = "invalid_grant";
    public static final int ONETHOUSAND = 1000;
    public static final int TWOTHOUSAND = 2000;
    public static final String CURRENCY_EUR = "EUR";
    public static final String LIMIT = "limit";

    public static class URLS {
        public static final String HOST = "https://api.tech26.de";
        public static final String AUTHENTICATION =  "/oauth/token";
        public static final String ACCOUNT = "/api/accounts";
        public static final String TRANSACTION =  "/api/smrt/transactions";
        public static final String PENDING_TRANSACTION = "/api/pending/transactions";
        public static final String SAVINGS = "/api/hub/savings/accounts";
        public static final String SAVINGS_FIXEDTERMS = "/api/hub/savings/fixedterms/accounts";
        public static final String SAVINGS_OPTIONS = "/api/hub/savings/options";
        public static final String LOGOUT = "/api/me/logout";
        public static final String CREDIT_DRAFTS = "/api/hub/credit/drafts";
        public static final String CREDIT_ELIGIBILITY = "/api/hub/credit/eligibility";
        public static final String USER_FULL_INFO = "/api/me";
    }

    public static class Storage {
        public static final String TOKEN_ENTITY = "TOKEN_ENTITY";
    }

    public static class Queryparams {
        public static final String FLOW_VERSION = "flowVersion";
        public static final String FLOW_VERSION_V2 = "V2";
        public static final String FULL = "full";
        public static final String FULL_TRUE = "true";
        public static final String LASTID = "lastId";
        public static final String LIMIT = "limit";
        public static final String LIMIT_DEFAULT = "100";
    }
    
    public static class Body {
        public static final String PASSWORD = "&password=";
        public static final String USERNAME = "&username=";
    }

    public static class Logging {
        public static final LogTag ACCOUNT_LOGGING = LogTag.from("#n26_account_logging");
    }
}
