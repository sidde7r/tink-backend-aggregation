package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import se.tink.backend.aggregation.nxgen.http.URL;

public class DemoFakeBankConstants {
    public static class Urls {
        private static final URL BASE_URL = new URL("demo-financial-institute.internal.staging.aggregation.tink.network:32011");
        //private static final URL BASE_URL = new URL("localhost:8090");
        public static final URL AUTHENTICATE_URL = BASE_URL.concatWithSeparator("authenticate");
    }

    public static class Storage {
        public static final String AUTH_TOKEN = "auth_token";
    }

    public static class Responses {
        public static final String SUCCESS_STRING = "SUCCESS";
    }
}
