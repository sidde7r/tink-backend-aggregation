package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

public class CommerzbankConstants {

    public static class URLS {
        public static final String HOST = "https://app.commerzbank.de";
        public static final String LOGIN = "/app/lp/v4/applogin";
        public static final String OVERVIEW = "/app/rest/v3/financeoverview";
        public static final String TRANSACTIONS = "/app/rest/transactionoverview";

    }

    public static class HEADERS {
        public static final String COOKIE = "Cookie";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CCB_CLIENT_VERSION = "CCB-Client-Version";
        public static final String USER_AGENT = "User-Agent";
        public static final String PRODUCT_TYPE = "productType";
        public static final String IDENTIFIER = "identifier";
        public static final String CONTENT_LENGTH = "Content-Length";
    }

    public static class VALUES {
        public static final String JSON = "application/json";
        public static final String CCB_VALUE = "MobBkniOS+10.0.0+10.3.1";
        public static final String USER_AGENT_VALUE = "MobBkniOS-10.0.0";
        public static final String CURRENT_ACCOUNT = "CurrentAccount";
    }

}
