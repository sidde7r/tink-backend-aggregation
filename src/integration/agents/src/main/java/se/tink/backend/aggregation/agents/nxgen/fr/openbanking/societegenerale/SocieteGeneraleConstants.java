package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SocieteGeneraleConstants {

    public static final String INTEGRATION_NAME = "societegenerale";

    public static class Urls {
        private static final String BASE_AUTH = "https://particuliers.sg-signin.societegenerale.fr";
        public static final String BASE_URL =
                "https://mtls.api.societegenerale.fr/sg/prod/pri/v1.4.2.4/psd2/xs2a";

        public static final String AUTHORIZE_PATH = BASE_AUTH + "/oauth2/authorize";
        public static final String TOKEN_PATH =
                "https://mtls.sg-signin.societegenerale.fr/oauth2/token";

        public static final URL ACCOUNTS_PATH = new URL(BASE_URL + "/accounts");
        public static final URL TRANSACTIONS_PATH =
                new URL(BASE_URL + "/accounts/{accountResourceId}/transactions");
        public static final URL END_USER_IDENTITY_PATH = new URL(BASE_URL + "/end-user-identity");
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String SCOPE = "scope";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CLIENT_ID = "client-id";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String SIGNATURE = "signature";
    }

    public static class HeaderValues {
        public static final String BEARER = "Bearer";
        public static final String BASIC = "Basic";
    }

    public class QueryValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "aisp";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class ErrorMessages {
        public static final String MISSING_CONFIGURATION =
                "Missing configuration for Societe Generale";
        public static final String MISSING_BALANCE = "Balance could not be found";
    }

    public class StorageKeys {
        public static final String TOKEN = "STRING_TOKEN";
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
    }

    public static class SignatureKeys {
        public static final String KEY_ID = "keyId";
        public static final String HEADERS = "headers";
    }

    public static class SignatureValues {
        public static final String HEADERS = "x-request-id";
        public static final String RSA_SHA256 = "rsa-sha256";
        public static final String ALGORITHM = "algorithm";
    }

    public static class IdTags {
        public static final String ACCOUNT_RESOURCE_ID = "accountResourceId";
    }
}
