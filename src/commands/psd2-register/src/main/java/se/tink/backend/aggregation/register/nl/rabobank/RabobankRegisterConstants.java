package se.tink.backend.aggregation.register.nl.rabobank;

import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class RabobankRegisterConstants {
    public static class Url {
        public static final URL REGISTER =
                new URL("https://api.rabobank.nl/openapi/open-banking/third-party-providers");
        public static final URL EIDAS_PROXY_BASE_URL =
                new URL("https://eidas-proxy.staging.aggregation.tink.network");
    }

    public static EidasProxyConfiguration eidasProxyConf =
            new EidasProxyConfiguration(Url.EIDAS_PROXY_BASE_URL.get(), true);

    public static class Cli {
        public static final String CERTIFICATE_PATH = "c";
        public static final String EMAIL = "e";
        public static final String ORGANIZATION = "o";
        public static final String CERTIFICATE_ID = "i";
    }

    public static class Header {
        public static final String ENROLLMENT_CLIENT_ID_KEY = "x-ibm-client-id";

        // Client ID used specifically for enrollment:
        // https://developer.rabobank.nl/reference/third-party-providers/1-0-0
        public static final String ENROLLMENT_CLIENT_ID = "64f38624-718d-4732-b579-b8979071fcb0";
    }
}
