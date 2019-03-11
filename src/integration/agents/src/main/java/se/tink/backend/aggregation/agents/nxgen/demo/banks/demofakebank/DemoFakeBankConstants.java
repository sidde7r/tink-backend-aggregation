package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import se.tink.backend.aggregation.nxgen.http.URL;

public class DemoFakeBankConstants {
    public static class Urls {
        // TODO: Do a proper solution for this before production!
        private static final URL BASE_URL =
                new URL("demo-financial-institute.internal.staging.aggregation.tink.network");
        // private static final URL BASE_URL = new URL("http://localhost:9271");
        public static final URL AUTHENTICATE_URL = BASE_URL.concat("/authenticate");
        public static final URL ACCOUNTS_URL = BASE_URL.concat("/accounts");
    }

    public static class Storage {
        public static final String AUTH_TOKEN = "auth_token";
        public static final String USERNAME = "username";
    }

    public static class Responses {
        public static final String SUCCESS_STRING = "SUCCESS";
    }
}
