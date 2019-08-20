package se.tink.backend.aggregation.register.fi.opbank;

import se.tink.backend.aggregation.nxgen.http.URL;

public final class OPBankRegisterConstants {
    public static class Url {
        public static final URL TPP_REGISTER =
                new URL("https://mtls.apis.op.fi/tpp-registration/register");
        public static final URL EIDAS_PROXY_URL =
                new URL("https://eidas-proxy.staging.aggregation.tink.network");
    }

    public static class Option {
        public static final String KEY_ID = "PSDSE-FINA-44059";
        public static final String TPP_ID = "SE-FINA-44059";
        public static final String ORGANIZATION_NAME = "Tink AB";
        public static final String CERTIFICATE_ID = "Tink";
        public static final String SOFTWARE_CLIENT_NAME = "Tink Aggregation";
        public static final String CLIENT_URI = "https://www.tink.se";
        public static final String[] SOFTWARE_ROLES = {"AIS", "PIS"};
        public static final String SOFTWARE_JWKS_ENDPOINT =
                "https://cdn.tink.se/eidas/jwks-pss.json";
        public static final String SOFTWARE_JWKS_REVOKED_ENDPOINT =
                "https://cdn.tink.se/eidas/jwks-pss.json";
        public static final String[] SOFTWARE_REDIRECT_URIS = {
            "https://main.staging.oxford.tink.se/api/v1/credentials/third-party/callback",
            "https://127.0.0.1:7357/api/v1/credentials/third-party/callback"
        };
        public static final String AUD = "https://mtls.apis.op.fi";
        public static final String GRANT_TYPES[] = {
            "client_credentials", "authorization_code", "refresh_token"
        };
    }

    private OPBankRegisterConstants() {
        throw new AssertionError();
    }
}
