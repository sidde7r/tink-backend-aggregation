package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution;

public class DemoFinancialInstitutionConstants {
    public static final String INTEGRATION_NAME = "demoFinancialInstitution";

    public static class Urls {
        public static final String ACCOUNTS = "/accounts";
        public static final String TRANSACTIONS = ACCOUNTS + "/{accountNumber}/transactions";
    }

    public static class Storage {
        public static final String BASIC_AUTH_USERNAME = "basic_auth_username";
        public static final String BASIC_AUTH_PASSWORD = "basic_auth_password";
    }

    public static class DemoFinancialInstitutionLoginCredentials {
        public static final String USER1_USERNAME = "199001011101";
        public static final String USER1_PASSWORD = "1234";

        public static final String USER2_USERNAME = "200001011101";
        public static final String USER2_PASSWORD = "1234";

        public static final String USER3_USERNAME = "191001011101";
        public static final String USER3_PASSWORD = "1234";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class QueryParams {
        public static final String PAGE_TOKEN = "pageToken";
    }
}
