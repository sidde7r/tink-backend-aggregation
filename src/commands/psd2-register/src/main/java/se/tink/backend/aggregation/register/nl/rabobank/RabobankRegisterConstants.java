package se.tink.backend.aggregation.register.nl.rabobank;

import se.tink.backend.aggregation.nxgen.http.URL;

public final class RabobankRegisterConstants {
    public static class Url {
        public static final URL REGISTER =
                new URL("https://api.rabobank.nl/openapi/open-banking/third-party-providers");
    }

    public static class Jwt {
        public static final String ORGANIZATION = "Tink AB";
    }

    public static class Cli {
        public static final String CERTIFICATE_PATH = "c";
        public static final String EMAIL = "e";
    }

    public static class Header {
        public static final String ENROLLMENT_CLIENT_ID_KEY = "x-ibm-client-id";

        // Client ID used specifically for enrollment:
        // https://developer.rabobank.nl/reference/third-party-providers/1-0-0
        public static final String ENROLLMENT_CLIENT_ID = "64f38624-718d-4732-b579-b8979071fcb0";
    }
}
