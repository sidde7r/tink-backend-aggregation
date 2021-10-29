package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea;

import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArkeaConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {
        public static final URL AUTHORISATION_PATH =
                new URL("https://openbanking.cmb.fr/authorize");
        public static final String BASE_PATH = "api-openbanking.cmb.fr";
        public static final String API_BASE_PATH = "https://" + BASE_PATH;
        public static final URL GET_AND_REFRESH_TOKEN_PATH =
                new URL(API_BASE_PATH + "/oauth-authorizationcode-psd2/token");
        public static final String API_PSD_BASE_PATH = API_BASE_PATH + "/psd2";
        public static final String ACCOUNTS_PATH = API_PSD_BASE_PATH + "/v1/accounts";
        public static final String BALANCES_PATH = "balances";
        public static final String TRANSACTIONS_PATH = "transactions";
        public static final String END_USER_IDENTITY_PATH =
                API_PSD_BASE_PATH + "/v1/end-user-identity";
        public static final String TRUSTED_BENEFICIARIES_PATH =
                API_PSD_BASE_PATH + "/v1/trusted-beneficiaries";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryValues {
        public static final String CODE_RESPONSE_TYPE = "code";
        public static final String AISP_EXTENDED_TRANSACTION_HISTORY_SCOPE =
                "aisp extended_transaction_history";
        public static final String STATE = "state";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderKeys {
        public static final String SIGNATURE = "signature";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String REQUEST_TARGET = "(request-target)";
        public static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderValues {
        public static final String APPLICATION_HAL_JSON_CHARSET_VERSION =
                "application/hal+json; charset=utf-8; version=1.4.2";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SignatureKeys {
        public static final String KEY_ID = "keyId";
        public static final String HEADERS = "headers";
        public static final String ALGORITHM = "algorithm";
        public static final String SIGNATURE = "signature";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SignatureValues {
        public static final String RSA_SHA256 = "rsa-sha256";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FormKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String AUTHORIZATION_CODE = "authorization_code";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StorageKeys {
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
    }
}
