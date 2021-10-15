package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class BankDataConstants {

    public static class Urls {
        public static final String INIT_AUTH = "/oauth-authorize";
        public static final String VALIDATE_NEMID = "/authentication/nemid_bank_twofactor";
        public static final String CLIENT_SECRET = "/client-registration";
        public static final String OAUTH_TOKEN = "/oauth-token";
        public static final String AUTH_CHALLENGE = "/challenge/v1/ropc/";
        public static final String FETCH_ACCOUNTS = "/rel/micro/accounts";
        public static final String FETCH_TRANSACTIONS = "/rel/micro/transactions/booked";
        public static final String FETCH_INVESTMENTS = "/rel/investment/accounts/overview";
        public static final String FETCH_MORTGAGES = "/rjb/living/loans/";
        public static final String FETCH_IDENTITY = "/rel/general/userprofiles/name";
        public static final String VALIDATE_VERSION = "/rel/unauth/version/validate/51";
        public static final String SERVER_STATUS = "/rel/unauth/server/status";
        public static final String GENERAL_HEALTH = "/rel/general/health";
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
        public static final String ACCEPT_HTML =
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        public static final String ACCEPT_JSON = "application/vnd.relationsbank-v3+json";
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
        public static final String CLASSIFICATIONS = "classifications";
        public static final String LISTINGS = "listings";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "token";
        public static final String CLIENT_ID = "relationsbank_native_implicit";
        public static final String SCOPE = "dcr";
        public static final String RESPONSE_MODE = "web_message";
        public static final String UI_LOCALES = "da";
        public static final String CLASSIFICATIONS = "misc";
        public static final String LISTINGS = "securities,custodyAccounts,poolAccounts,favorites";
    }

    public static class Storage {
        public static final String CODE_VERIFIER = "codeVerifier";
        public static final String TOKEN = "token";
        public static final String USER_ID = "userId";
        public static final String PIN_CODE = "pin";
        public static final String KEY_ID = "keyId";
        public static final String KEY_PAIR = "keyPair";
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String REFRESH_TOKEN = "refreshToken";
        public static final String PUBLIC_ID = "publicId";
        public static final String COUNTER = "counter";
        public static final String ACCOUNT_RESPONSE = "accountResponse";
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
        public static final String CHAL = "chal";
        public static final String COUNT = "count";
    }

    public static class JwtValues {
        public static final String KTY = "RSA-2048";
        public static final String APP = "Relationsbank";
        public static final String AOS = "iPhone web 13.3.1";
        public static final String ENROLLMENT_TYPE = "enrollment";
        public static final String LOGIN_TYPE = "login";
    }

    public static class FormKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String GRANT_TYPE = "grant_type";
        public static final String PASSWORD = "password";
        public static final String SCOPE = "scope";
        public static final String USERNAME = "username";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String PASSWORD = "password";
        public static final String REPLAY_ID = "digitalbanking replayId:";
        public static final String NOT_USED = "NOT_USED";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class HttpClient {
        public static final int MAX_RETRIES = 3;
        public static final int RETRY_SLEEP_MILLISECONDS = 2000;
    }

    public static final class Log {
        public static final LogTag UNKOWN_ACCOUNT_TYPE =
                LogTag.from("#dk_jyske_unknown_account_type");
    }

    public static class Fetcher {
        public static final int START_PAGE = 0;
    }
}
