package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BredBanquePopulaireConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {
        public static final String BASE_URL = "https://api.bred.fr/psd2-aisp/v1";
        public static final String OAUTH_URL =
                "https://oauthclient.bred.fr/bred-auth-client/oauth/authorize";
        public static final String GET_TOKEN =
                "https://api.bred.fr/bred-auth-client/v1/oauth/token";
        public static final String FETCH_ACCOUNTS = BASE_URL + "/accounts";
        public static final String FETCH_BALANCES = BASE_URL + "/accounts/%s/balances";
        public static final String CUSTOMERS_CONSENTS = BASE_URL + "/consents";
        public static final String FETCH_TRUSTED_BENEFICIARIES =
                BASE_URL + "/trusted-beneficiaries";
        public static final String FETCH_TRANSACTIONS = BASE_URL + "/accounts/%s/transactions";
        public static final String FETCH_IDENTITY_DATA = BASE_URL + "/end-user-identity";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "%s?state=%s";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "aisp";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String REDIRECT_URL = "redirect_url";
        public static final String DIGEST = "digest";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderGeneratorKeys {
        public static final String ALGORITHM = "rsa-sha256";
        public static final String PUT_HEADERS_KEYS = "x-request-id digest content-type";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ConsentValues {
        public static final String SCHEME_NAME = "BRED";
        public static final String ISSUER = "BREDFRPPXXX";
        public static final String CURRENCY = "EUR";
    }
}
