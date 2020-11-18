package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class NordeaDkConstants {

    public static class URLs {
        public static final String NEM_ID_AUTHENTICATION =
                "api/dbf/ca/nemid-v1/nemid/authentications/";
        public static final String AUTHORIZATION =
                "api/dbf/ca/user-accounts-service-v1/user-accounts/primary/authorization";
        public static final String NORDEA_AUTH_BASE_URL = "https://identify.nordea.com/";
        public static final String NORDEA_PRIVATE_BASE_URL = "https://private.nordea.dk/";
        public static final String FETCH_ACCOUNTS =
                NORDEA_PRIVATE_BASE_URL + "api/dbf/ca/accounts-v3/accounts";
        public static final String FETCH_LOANS =
                NORDEA_PRIVATE_BASE_URL + "api/dbf/ca/loans-v1/loans";
        public static final String EXCHANGE_TOKEN =
                NORDEA_AUTH_BASE_URL + "api/dbf/ca/token-service-v3/oauth/token";
        public static final String TRANSACTIONS = "/%s/transactions";
        public static final String FETCH_ACCOUNT_TRANSACTIONS_FORMAT =
                FETCH_ACCOUNTS + TRANSACTIONS;
        public static final String FETCH_CARDS =
                NORDEA_PRIVATE_BASE_URL + "api/dbf/ca/cards-v4/cards";
        public static final String FETCH_CARD_DETAILS_FORMAT = FETCH_CARDS + "/%s";
        public static final String FETCH_CARD_TRANSACTIONS_FORMAT = FETCH_CARDS + TRANSACTIONS;
        public static final String FETCH_INVESTMENTS =
                NORDEA_PRIVATE_BASE_URL + "/api/dbf/ca/savings-v1/savings/custodies";
    }

    public static class QueryParamKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String UI_LOCALES = "ui_locales";
        public static final String AV = "av";
        public static final String DM = "dm";
        public static final String INSTALLED_APPS = "installed_apps";
        public static final String SCOPE = "scope";
        public static final String LOGIN_HINT = "login_hint";
        public static final String APP_CHANNEL = "app_channel";
        public static final String ADOBE_MC = "adobe_mc";
        public static final String NONCE = "nonce";
    }

    public static class QueryParamValues {

        public static final String CLIENT_ID = "CDi170IiCEmvEbxWn3Hk";
        public static final String CODE_CHALLENGE_METHOD = "S256";
        public static final String REDIRECT_URI = "com.nordea.MobileBankDK://auth-callback";
        public static final String RESPONSE_TYPE = "code";
        public static final String UI_LOCALES = "en";
        public static final String AV = "3.10.0.922";
        public static final String DM = "iPhone7,2";
        public static final String INSTALLED_APPS = "bankid";
        public static final String SCOPE = "openid ndf agreement";
        public static final String LOGIN_HINT = "nemid_2f";
        public static final String APP_CHANNEL = "NDM_DK_IOS";
        public static final String ADOBE_MC =
                "06154807650644269292603132926226903218|MCORGID=9D193D565A0AFF460A495E66%40AdobeOrg|TS=1582024036";
    }

    public static class HeaderKeys {
        public static final String HOST = "Host";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        public static final String REFERER = "Referer";
        public static final String APP_VERSION = "x-app-version";
        public static final String DEVICE_MODEL = "x-device-model";
        public static final String APP_COUNTRY = "x-app-country";
        public static final String PLATFORM_TYPE = "x-platform-type";
        public static final String ORIGIN = "Origin";
        public static final String ACCEPT_LANGUAGE = "Accept-Language";
        public static final String APP_LANGUAGE = "x-app-language";
        public static final String PLATFORM_VERSION = "x-platform-version";
        public static final String APP_SEGMENT = "x-app-segment";
        public static final String DEVICE_ID = "x-Device-Id";
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_AUTHORIZATION = "x-Authorization";
    }

    public static class HeaderValues {
        public static final String NORDEA_AUTH_HOST = "identify.nordea.com";
        public static final String NORDEA_PRIVATE_HOST = "private.nordea.dk";
        public static final String TEXT_HTML =
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        public static final String BR_GZIP_ENCODING = "br, gzip, deflate";
        public static final String APP_VERSION = "3.10.0.922 -> 1.13.0";
        public static final String DEVICE_MODEL = "iPhone7,2";
        public static final String PLATFORM_TYPE = "iOS";
        public static final String APP_LANGUAGE = "en_DK";
        public static final String APP_COUNTRY = "DK";
        public static final String HOUSEHOLD_APP_SEGMENT = "household";
        public static final String PLATFORM_VERSION = "12.4.3";
        public static final String ACCEPT_LANGUAGE = "en-DK";
    }

    public static class NordeaNemIdLocale {
        private static final List<String> SUPPORTED_LOCALES = Arrays.asList("en", "da");

        public static final String DEFAULT_LOCALE = "en";

        public static boolean isUserLocaleSupported(String userLocale) {
            return SUPPORTED_LOCALES.stream()
                    .anyMatch(supportedLocale -> supportedLocale.equalsIgnoreCase(userLocale));
        }
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = "token";
        public static final String NEMID_TOKEN = "nemIdToken";
        public static final String SESSION_ID = "sessionId";
        public static final String REFERER = "referer";
        public static final String CODE_VERIFIER = "codeVerifier";
        public static final String NONCE = "nonce";
        public static final String DEVICE_ID = "deviceId";
        public static final String PRODUCT_CODE = "product_code";
    }

    public static class FormKeys {
        public static final String COUNTRY = "country";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String LOGIN_HINT = "login_hint";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String AUTH_METHOD = "auth_method";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SCOPE = "scope";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String COUNTRY = "DK";
        public static final String CLIENT_ID = "CDi170IiCEmvEbxWn3Hk";
        public static final String REDIRECT_URI = "com.nordea.MobileBankDK://auth-callback";
        public static final String AUTH_METHOD = "nasa";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String SCOPE = "ndf";
    }

    public static final String CURRENCY = "DKK";

    public static TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "transaction")
                    .put(AccountTypes.SAVINGS, "savings")
                    .put(AccountTypes.CREDIT_CARD, "credit", "combined")
                    .put(AccountTypes.LOAN, "mortgage")
                    .build();
}
