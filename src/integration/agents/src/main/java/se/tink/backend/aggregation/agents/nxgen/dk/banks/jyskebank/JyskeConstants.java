package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class JyskeConstants {

    public static class Urls {
        public static final String AUTH_HOST = "https://auth.jyskebank.dk";
        private static final String HOST = "https://api.jyskebank.dk";

        public static final String INIT_AUTH = AUTH_HOST + "/oauth-authorize";
        public static final String VALIDATE_NEMID =
                AUTH_HOST + "/authentication/nemid_bank_twofactor";
        public static final String CLIENT_SECRET = AUTH_HOST + "/client-registration";
        public static final String OAUTH_TOKEN = AUTH_HOST + "/oauth-token";
        public static final String FETCH_ACCOUNTS = HOST + "/rel/micro/accounts";
        public static final String FETCH_TRANSACTIONS = HOST + "/rel/micro/transactions/booked";
        public static final String FETCH_IDENTITY = HOST + "/rel/general/userprofiles/name";
        public static final String VALIDATE_VERSION = HOST + "/rel/unauth/version/validate/51";
        public static final String SERVER_STATUS = HOST + "/rel/unauth/server/status";
        public static final String GENERAL_HEALTH = HOST + "/rel/general/health";
    }

    public static class HeaderKeys {
        public static final String ORIGIN = "Origin";
        public static final String REFERER = "Referer";
        public static final String BUILD_NUMBER = "x-app-build-number";
        public static final String CORRELATION_ID = "bd-correlation-id";
        public static final String API_KEY = "x-api-key";
        public static final String KEY_ID = "proc-enrollmentKeyId";
        public static final String CORR_ID = "corrid";
        public static final String APP_VERSION = "x-app-version";
        public static final String BD_CORRELATION = "bd-correlation-id";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class HeaderValues {
        public static final String REFERER =
                "https://auth.jyskebank.dk/authentication/nemid_bank_twofactor";
        public static final String ACCEPT_LANGUAGE = "en-us";
        public static final String ACCEPT =
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        public static final String USER_AGENT = "JyskeBank/2.21.0 (iPhone; iOS 13.3.1; Scale/3.00)";
        public static final String BUILD_NUMBER = "111";
        public static final String API_KEY = "w6FW248sXt42WZaaq8boFmXMGGTu06AG";
        public static final String APP_VERSION = "2.21.0.111";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String RESPONSE_MODE = "response_mode";
        public static final String UI_LOCALES = "ui_locales";
        public static final String ENROLLMENT_CHALLENGE = "enrollment_challenge";
        public static final String PUBLIC_ID = "publicid";
        public static final String PAGE = "page";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "token";
        public static final String CLIENT_ID = "relationsbank_native_implicit";
        public static final String SCOPE = "dcr";
        public static final String REDIRECT_URI = "drb://drb.jyskebank.dk/dcr-callback.html";
        public static final String RESPONSE_MODE = "web_message";
        public static final String UI_LOCALES = "da";
    }

    public static class Storage {
        public static final String CODE_VERIFIER = "codeVerifier";
        public static final String TOKEN = "token";
        public static final String USER_ID = "userId";
        public static final String PIN_CODE = "pin";
        public static final String KEY_ID = "keyId";
        public static final String PUBLIC_KEY = "publicKey";
        public static final String PRIVATE_KEY = "privateKey";
        public static final String AES_KEY = "aesKey";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String REFRESH_TOKEN = "refreshToken";
        public static final String PUBLIC_ID = "publicId";
    }

    public static class Crypto {
        public static final String PUBLIC_KEY =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtTjrzhF8ph9twd4gFxKgeiAjkrHjfbgdQhjYGPChnu5GC0Fnb2oemtEoOvdA3l5X2r8VYK+ffGHSB0LVLR4TxDRd3JzocbC9qkmMm3gfU43rbQebKtNzBZJAFTEPX7DQObVt3RBhTvpVpuOCyWwzO1HFf6kBVONDX/kY6Wqcda2UOgIc4JttWCyiV13KaktmXUc1dbRE883lueLcVTl+c7geYSgrQbljP/yZx7x0BB3Og6jjGLrbqBrvXrfxrF3TmCazLxvQspN95kyd9pZszMArZcQY8EX3xWf18KHENibAuYyjHZfCaOqQWNBEY7GSBA8rgAuGnnfuu6ZI8KX18QIDAQAB";
        public static final String KEY_ID = "X509CAP.BDCA.APPBK051.PRI00000";
    }

    public static class Authentication {
        public static final String NEM_ID_IFRAME_FORMAT =
                "<iframe id=\"nemid_iframe\" title=\"NemID\" scrolling=\"no\" frameborder=\"0\"  src=\"%s\"></iframe>";
    }

    public static class JwtKeys {
        public static final String PWD = "pwd";
        public static final String KEY_ID = "keyid";
        public static final String ECHA = "echa";
        public static final String PUB = "pub";
        public static final String KTY = "kty";
        public static final String APP = "app";
        public static final String AOS = "aos";
    }

    public static class JwtValues {
        public static final String KTY = "RSA-2048";
        public static final String APP = "Relationsbank";
        public static final String AOS = "iPhone web 13.3.1";
        public static final String TYPE = "enrollment";
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder().put(AccountTypes.CHECKING, "7248").build();
}
