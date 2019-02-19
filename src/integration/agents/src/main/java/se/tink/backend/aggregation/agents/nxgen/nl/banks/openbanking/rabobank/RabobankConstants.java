package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;

public class RabobankConstants {

    public class URL {
        private static final String API_RABOBANK =
                "https://api-sandbox.rabobank.nl/openapi/sandbox";
        public static final String OAUTH2_RABOBANK = API_RABOBANK + "/oauth2";
        public static final String PAYMENTS_RABOBANK = API_RABOBANK + "/payments";
        public static final String AUTHORIZE_RABOBANK = OAUTH2_RABOBANK + "/authorize";
        public static final String OAUTH2_TOKEN_RABOBANK = OAUTH2_RABOBANK + "/token";
        public static final String AIS_RABOBANK_ACCOUNTS =
                PAYMENTS_RABOBANK + "/account-information/ais/v3/accounts";
    }

    public class StorageKey {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String REDIRECT_URL = "redirect_url";
        public static final String CLIENT_CERT = "client_cert";
        public static final String CLIENT_CERT_KEY = "client_cert_key";
        public static final String CLIENT_CERT_KEY_PASSWORD = "client_cert_key_password";
    }

    public class QueryParams {
        public static final String CLIENT_ID = "client_id";
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String DIGEST = "Digest";
        public static final String SIGNATURE = "Signature";
        public static final String DATE = "Date";
    }

    public class QueryValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String CODE = "code";
    }
}
