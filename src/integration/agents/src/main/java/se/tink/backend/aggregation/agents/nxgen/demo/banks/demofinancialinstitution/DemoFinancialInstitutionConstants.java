package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution;

public class DemoFinancialInstitutionConstants {
    public static final String INTEGRATION_NAME = "demoFinancialInstitution";

    public static class Urls {
        public static final String AUTHENTICATE = "/authenticate";
        public static final String ACCOUNTS = "/accounts";
    }

    public static class Storage {
        public static final String AUTH_TOKEN = "auth_token";
        public static final String USERNAME = "username";
    }

    public static class Responses {
        public static final String SUCCESS_STRING = "SUCCESS";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }
}
