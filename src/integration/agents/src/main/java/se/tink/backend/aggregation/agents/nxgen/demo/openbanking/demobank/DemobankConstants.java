package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

public class DemobankConstants {
    public static class Urls {
        public static final String LOGIN = "/api/login";
        public static final String OAUTH_TOKEN = "/oauth/token";
        public static final String ACCOUNTS = "/api/accounts";
        public static final String ACCOUNT_DETAILS = "/api/account/{accountId}/details";
        public static final String TRANSACTIONS = "/api/account/{accountId}/transactions";
    }
}
