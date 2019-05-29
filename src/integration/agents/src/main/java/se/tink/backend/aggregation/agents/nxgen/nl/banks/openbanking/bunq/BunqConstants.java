package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import se.tink.backend.aggregation.nxgen.http.URL;

public final class BunqConstants {

    private BunqConstants() {}

    public static final class Urls {
        public static final URL TOKEN_EXCHANGE =
                new URL("https://api-oauth.sandbox.bunq.com/v1/token");
        public static final URL AUTHORIZE = new URL("https://oauth.sandbox.bunq.com/auth");
    }

    public static final class QueryParams {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static final class QueryValues {
        public static final String CODE = "code";
        public static final String AUTHORIZATION_CODE = "authorization_code";
    }

    public static final class StorageKeys {
        public static final String PSD2_CLIENT_AUTH_TOKEN = "PSD2clientAuthToken";
        public static final String PSD2_SESSION_TIMEOUT = "PSD2-session-timeout";
        public static final String PSD2_DEVICE_RSA_SIGNING_KEY_PAIR = "PSD2deviceRsaSigningKeyPair";
    }

    public static final class Market {
        public static final String INTEGRATION_NAME = "bunq";
        public static final String CLIENT_NAME = "tink";
    }
}
