package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

public class DemoFakeBankConstants {
    public static final String INTEGRATION_NAME = "demofakebank";

    public static class Urls {
        public static final String DEFAULT_BASE_URL = "demo-financial-institute.internal.staging.aggregation.tink.network";
        public static final String AUTHENTICATE = "/authenticate";
        public static final String ACCOUNTS = "/accounts";
    }

    public static class Storage {
        public static final String AUTH_TOKEN = "auth_token";
        public static final String USERNAME = "username";
        public static final String BASE_URL = "base_url";
    }

    public static class Responses {
        public static final String SUCCESS_STRING = "SUCCESS";
    }
}
