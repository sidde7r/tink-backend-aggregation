package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1;

public class OAuth1Constants {
    public static class PersistentStorageKeys {
        public static final String OAUTH_VERIFIER = "oauth_verifier";
        public static final String OAUTH_TOKEN = "oauth_token";
    }

    public static class CallbackParams {
        public static final String OAUTH_VERIFIER = "oauth_verifier";
        public static final String OAUTH_TOKEN = "oauth_token";
    }

    public static class QueryParams {
        public static final String OAUTH_CALLBACK = "oauth_callback";
        public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
        public static final String OAUTH_NONCE = "oauth_nonce";
        public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
        public static final String OAUTH_VERSION = "oauth_version";
        public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
        public static final String OAUTH_SIGNATURE = "oauth_signature";
        public static final String OAUTH_VERIFIER = "oauth_verifier";
        public static final String OAUTH_TOKEN = "oauth_token";
    }

    public static class QueryValues {
        public static final String HMAC_SHA1 = "HMAC-SHA1";
        public static final String VERSION = "1.0";
        public static final String OAUTH = "OAuth";
    }
}
