package se.tink.backend.aggregation.register;

public final class PSD2RegisterConstants {

    private PSD2RegisterConstants() {}

    public static final class RedirectUrls {
        public static final String PRODUCTION_CALLBACK_URL =
                "https://api.tink.se/api/v1/credentials/third-party/callback";
        public static final String STAGING_CALLBACK_URL =
                "https://staging.oxford.tink.se/api/v1/credentials/third-party/callback";
        public static final String LOCAL_CALLBACK_URL =
                "https://127.0.0.1:7357/api/v1/credentials/third-party/callback";
    }
}
