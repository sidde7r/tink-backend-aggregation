package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class SpardaConstants {

    public static final boolean SANDBOX = false;

    public static class Urls {

        private static final String BASE_PROD = "https://api.sparda.de:443/xs2a/3.0.0/v1";
        private static final String BASE_SANDBOX = "https://api-mock.sparda.de/mock/3.0.0/v1";

        private static final String BASE_AUTH_PROD = "https://idp.sparda.de";
        private static final String BASE_AUTH_SANDBOX = "https://idp-mock.sparda.de";

        private static final String BASE_HOST = SANDBOX ? BASE_SANDBOX : BASE_PROD;
        private static final String AUTH_HOST = SANDBOX ? BASE_AUTH_SANDBOX : BASE_AUTH_PROD;

        public static final URL TOKEN = new URL(AUTH_HOST + "/oauth2/token");
        public static final URL CONSENTS = new URL(BASE_HOST + "/consents");
        public static final URL CONSENT = new URL(BASE_HOST + "/consents/{consentId}");

        public static final URL ACCOUNTS = new URL(BASE_HOST + "/accounts");
        public static final URL TRANSACTIONS =
                new URL(BASE_HOST + "/accounts/{accountId}/transactions");
    }

    public static final String SANDBOX_CODE_CHALLENGE =
            "lRPHsFD6rWW1zJlodkYWMRdV0K9uY29EXe_L7ZM_SZc";
}
