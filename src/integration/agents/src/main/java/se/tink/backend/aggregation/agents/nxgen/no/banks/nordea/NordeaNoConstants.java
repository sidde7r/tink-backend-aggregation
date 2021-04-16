package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NordeaNoConstants {

    public static final String PRODUCT_CODE = "productCode";
    public static final String CAN_FETCH_TRANSACTION = "canFetchTransactions";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlLocators {
        public static final BankIdElementLocator LOC_BANK_ID_METHOD_BUTTON =
                BankIdElementLocator.builder()
                        .element(new By.ByCssSelector("span.method-item__info__title"))
                        .mustHaveExactText("BankID")
                        .build();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryParamKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String PLATFORM = "platform";
        public static final String METHOD_ID = "method_id";
        public static final String LANG = "lang";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String AV = "av";
        public static final String DM = "dm";
        public static final String EC = "ec";
        public static final String INTEGRATION_URL = "integration_url";
        public static final String SCOPE = "scope";
        public static final String LOGIN_HINT = "login_hint";
        public static final String NONCE = "nonce";
        public static final String PRODUCT_CODE = "product_code";
        public static final String CONTINUATION_KEY = "continuation_key";
        public static final String SESSION_ID = "session_id";
        public static final String PAGE = "page";
        public static final String PAGE_SIZE = "page_size";
        public static final String TYPE = "type";
        public static final String AUTHORIZATION_CODE = "code";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryParamValues {
        public static final String CLIENT_ID = "bQwEsS8eczQFjqqF2ocl";
        public static final String CODE_CHALLENGE_METHOD = "S256";
        public static final String REDIRECT_URI = "com.nordea.mobilebank.no://auth-callback";
        public static final String PLATFORM = "iOS";
        public static final String METHOD_ID = "bankidm_no";
        public static final String LANG = "en";
        public static final String RESPONSE_TYPE = "code";
        public static final String AV = "3.13.2.1092";
        public static final String DM = "iPhone9,3";
        public static final String EC = "0";
        public static final String SCOPE = "openid ndf agreement offline_access";
        public static final String LOGIN_HINT_SHORT_FORMAT = "BIM:%s:%s";
        public static final String PAGE_SIZE = "100";
        public static final String TYPE = "TOTAL";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderKeys {
        public static final String HOST = "Host";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        public static final String REFERER = "Referer";
        public static final String APP_VERSION = "x-app-version";
        public static final String DEVICE_MODEL = "x-device-model";
        public static final String APP_COUNTRY = "x-app-country";
        public static final String PLATFORM_TYPE = "x-platform-type";
        public static final String ACCEPT_LANGUAGE = "Accept-Language";
        public static final String APP_LANGUAGE = "x-app-language";
        public static final String PLATFORM_VERSION = "x-platform-version";
        public static final String APP_SEGMENT = "x-app-segment";
        public static final String DEVICE_EC = "x-device-ec";
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_AUTHORIZATION = "x-Authorization";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderValues {
        public static final String NORDEA_PRIVATE_HOST = "private.nordea.no";
        public static final String BR_GZIP_ENCODING = "br, gzip, deflate";
        public static final String APP_VERSION = "3.13.2.1092 -> 1.16.3";
        public static final String APP_VERSION_SHORT = "3.13.2.1092";
        public static final String DEVICE_MODEL = "iPhone9,3";
        public static final String DEVICE_EC = "0";
        public static final String PLATFORM_TYPE = "iOS";
        public static final String APP_LANGUAGE = "en_NO";
        public static final String APP_COUNTRY = "NO";
        public static final String HOUSEHOLD_APP_SEGMENT = "household";
        public static final String PLATFORM_VERSION = "12.4.0";
        public static final String ACCEPT_LANGUAGE = "en-no";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FormKeys {
        public static final String COUNTRY = "country";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String AUTH_METHOD = "auth_method";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SCOPE = "scope";
        public static final String TOKEN = "refresh_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String TOKEN_TYPE_HINT = "token_type_hint";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FormValues {
        public static final String COUNTRY = "NO";
        public static final String CLIENT_ID = "bQwEsS8eczQFjqqF2ocl";
        public static final String REDIRECT_URI = "com.nordea.mobilebank.no://auth-callback";
        public static final String AUTH_METHOD = "nasa";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String SCOPE = "ndf";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CLIENT_ID_REFRESH = "NDHMNO";
        public static final String TOKEN_TYPE_HINT = "access_token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UriParams {
        public static final String URI_ACCOUNT_ID = "accountId";
        public static final String URI_CARD_ID = "cardId";
        public static final String URI_LOAN_ID = "loanId";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {

        public static final String NORDEA_AUTH_BASE_URL = "https://identify.nordea.com/";
        public static final String NORDEA_PRIVATE_BASE_URL = "https://private.nordea.no/";

        public static final String NORDEA_AUTHENTICATION_START =
                NORDEA_AUTH_BASE_URL + "api/dbf/ca/bankidno-v1/authentications";
        public static final String BANKID_AUTHENTICATION_INIT = NORDEA_AUTH_BASE_URL + "bidno";

        public static final String NORDEA_REDIRECT_BACK_TO_MOBILE_APP_URL =
                "https://identify.nordea.com/redirect";

        public static final String EXCHANGE_TOKEN =
                NORDEA_PRIVATE_BASE_URL + "api/dbf/ca/token-service-v3/oauth/token";
        public static final String LOGOUT =
                NORDEA_PRIVATE_BASE_URL + "api/dbf/ca/token-service-v3/oauth/token/revoke";

        public static final String FETCH_ACCOUNTS =
                NORDEA_PRIVATE_BASE_URL + "api/dbf/ca/accounts-v2/accounts";
        public static final String FETCH_ACCOUNT_TRANSACTIONS =
                NORDEA_PRIVATE_BASE_URL
                        + "api/dbf/ca/accounts-v2/accounts/{accountId}/transactions";

        public static final String FETCH_IDENTITY_DATA =
                NORDEA_PRIVATE_BASE_URL + "/api/dbf/no/customerinfo-v2/customers/info";

        public static final String FETCH_CARDS =
                NORDEA_PRIVATE_BASE_URL + "api/dbf/ca/cards-v4/cards";
        public static final String FETCH_CARD_DETAILS =
                NORDEA_PRIVATE_BASE_URL + "api/dbf/ca/cards-v4/cards/{cardId}";
        public static final String FETCH_CARD_TRANSACTIONS =
                NORDEA_PRIVATE_BASE_URL + "api/dbf/ca/cards-v4/cards/{cardId}/transactions";

        public static final String FETCH_INVESTMENTS =
                NORDEA_PRIVATE_BASE_URL + "/api/dbf/ca/savings-v1/savings/custodies";

        public static final String FETCH_LOANS =
                NORDEA_PRIVATE_BASE_URL + "/api/dbf/ca/loans-v1/loans";
        public static final String FETCH_LOAN_DETAILS =
                NORDEA_PRIVATE_BASE_URL + "/api/dbf/ca/loans-v1/loans/{loanId}";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RetryFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int RETRY_SLEEP_MILLISECONDS = 5000;
    }
}
