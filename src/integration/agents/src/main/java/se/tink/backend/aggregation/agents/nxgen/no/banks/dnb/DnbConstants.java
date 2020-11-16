package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class DnbConstants {

    public static final String CHARSET = "UTF-8";
    public static final String DEFAULT_CURRENCY = "NOK";

    public final class ProductNumber {
        public static final String StockAccount = "6503";
    }

    public static final class QueryParam {
        public static final String PREVENT_CACHE = "request.preventCache";
        public static final String COUNT = "count";
        public static final String SYSTEM = "system";
        public static final String ID = "id";
        public static final String COOKIE_SUPPORT = "cookiesupport";
    }

    public final class PostParameter {
        public static final String SSN = "uid";
        public static final String START_PAGE = "startpage";
        public static final String USER_CONTEXT = "userCtx";
        public static final String PHONE_NUMBER = "mobilephonenumber";
    }

    public final class Header {
        public static final String REQUEST_WITH_KEY = "X-Requested-With";
        public static final String REQUEST_WITH_VALUE = "XMLHttpRequest";
        public static final String ORIGIN = "Origin";
        public static final String REFERER = "Referer";
        // OAuth
        public static final String METHOD_POST = "POST";
        public static final String METHOD_GET = "GET";
        public static final String OAUTH_CALLBACK_KEY = "oauth_callback";
        public static final String OAUTH_CALLBACK_VALUE_ANDROID = "oob";
        public static final String OAUTH_CONSUMER_KEY_KEY = "oauth_consumer_key";
        public static final String OAUTH_CONSUMER_KEY_VALUE = "Min Formue";
        public static final String OAUTH_NONCE_KEY = "oauth_nonce";
        public static final String OAUTH_SIGNATURE_METHOD_KEY = "oauth_signature_method";
        public static final String OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1";
        public static final String OAUTH_TIMESTAMP_KEY = "oauth_timestamp";
        public static final String OAUTH_VERSION_KEY = "oauth_version";
        public static final String OAUTH_VERSION_VALUE = "1.0";
        public static final String OAUTH_TOKEN_KEY = "oauth_token";
        public static final String OAUTH_VERIFIER_KEY = "oauth_verifier";
        public static final String OAUTH_SIGNATURE_KEY = "oauth_signature";
        public static final String FRIENDLY_NAME_KEY = "friendlyname";
        public static final String FRIENDLY_NAME_VALUE = "MyAndroid";
        public static final String AUTHORIZATION_HEADER = "Authorization";
    }

    public static final class Url {
        public static final String BASE_URL = "https://m.dnb.no";

        // Init
        public static final String INIT_LOGIN = BASE_URL + "/appo/logon/startmobile";
        public static final String INSTRUMENT_INFO =
                BASE_URL + "/segp/appo/logon/service_instrumentinfo";
        public static final String INIT_BANKID = BASE_URL + "/segp/appo/logon/service_bankid20init";
        public static final String CHALLENGE =
                BASE_URL + "/segp/appo/logon/service_initmobilebankid";

        // Collect
        public static final String COLLECT_BANKID =
                BASE_URL + "/segp/appo/logon/service_processmobilebankid";
        public static final String FINALIZE_LOGON =
                BASE_URL + "/segp/appo/logon/finalizelogon/?instrument=MobileBankIDFromOther";
        public static final String FIRST_REQUEST =
                BASE_URL + "/segp/ps/startsiden?firstrequestafterlogon=true";

        public static final String FETCH_ACCOUNT_DETAILS =
                BASE_URL + "/segp/apps/nbp/konto/integration/accounts/transactions";
        public static final String FETCH_TRANSACTIONS =
                BASE_URL + "/segp/apps/nbp/transaksjon/integration/transactions/detailref/%s";

        // Credit cards
        public static final String LIST_CARDS = BASE_URL + "/segp/apps/besok/cardlisting/list";
        public static final String GET_CARD = BASE_URL + "/segp/apps/kredittkort/rest/getCard/%s";
        public static final String FETCH_CARD_TRANSACTIONS =
                BASE_URL + "/segp/apps/kredittkort/rest/transactionsGrouped/%s";

        // OAuth
        public static final String GET_REQUEST_TOKEN =
                BASE_URL + "/appo/logon/oauth/getRequestToken";
        public static final String VERIFIER_SERVICE =
                BASE_URL + "/segp/apps/bidbest/oauth/verifierService";
        public static final String GET_ACCESS_TOKEN = BASE_URL + "/appo/logon/oauth/getAccessToken";
        public static final String INIT_MY_WEALTH = BASE_URL + "/api/mywealth/A/public/v2/init";

        // Investment
        public static final String GET_FUNDS_OVERVIEW =
                BASE_URL + "/api/mywealth/A/public/v2/fundoverview";
        public static final String GET_PENSION = BASE_URL + "/api/mywealth/A/public/ips/overview";
        public static final URL GET_FUND_DETAIL =
                new URL(BASE_URL + "/api/mywealth/A/public/v2/productdetail/{system}/{id}");
    }

    public final class OAuth {
        public static final String SIGN_ALGORITHM = "HmacSHA1";
        public static final String OAUTH_HEADER_PREFIX = "OAuth ";
        public static final String OAUTH_TOKEN_SECRET_KEY = "oauth_token_secret";
        // xiacheng NOTE: found in AndroidManifest
        public static final String DNB_API_SECRET = "156db93480evgxo8u83nif48nz";
    }

    public final class Messages {
        public static final String GENERIC_BANKID_ERROR =
                "du har ikke tilgang til å logge på med bankid. ta kontakt med din administrator.";
        public static final String LOGIN_TIMEOUT =
                "påloggingsforsøket ditt ble avbrutt på grunn av inaktivitet.";
        public static final String BANKID_TIMEOUT = "feilkode c302";
        public static final String BANKID_ALREADY_IN_PROGRESS = "feilkode c293";
        public static final String INCORRECT_PHONE_NUMER_OR_INACTIVATED_MOBILE_BANKID =
                "feilkode c161";
        public static final String BANKID_BLOCKED_A = "feilkode c176";
        public static final String BANKID_BLOCKED_B = "feilkode c30e";
        public static final String BANKID_BLOCKED_C = "feilkode c30f";
        public static final String BANKID_BLOCKED_D = "feilkode c307";
        public static final String ERROR_MOBILE_OPERATOR_A = "feilkode c102";
        public static final String ERROR_MOBILE_OPERATOR_B = "feilkode c202";
        public static final String ERROR_MOBILE_OPERATOR_C = "feilkode c30c";
        public static final String ERROR_MOBILE_OPERATOR_D = "feilkode c131";
        public static final String ERROR_MOBILE_OPERATOR_E = "feilkode c325";
        public static final String ERROR_MOBILE_OPERATOR_F = "feilkode c308";
        public static final String ERROR_MOBILE_OPERATOR_G = "feilkode c302";

        public static final String SSN_FORMAT_ERROR =
                "ukjent brukeridentitet eller feil format på brukeridentitet";
        public static final String USER_ID_BLOCKED =
                "Din bruker-ID er sperret. Kontakt oss for mer informasjon.";

        public static final String NO_ACCESS = "<div id=\"websealError\">no_access</div>";
        public static final String NO_ACCOUNT_SUFFIX = "ikke funnet";
        public static final String SERVER_UNAVAILABLE =
                "The server is temporarily unable to service your request";
        public static final String SERVICE_NOT_AVAILABLE_PREFIX = "Vi beklager at tjenesten";
        public static final String TRY_IN_A_FEW_MINUTES_PREFIX = "fem minutter";
        public static final String TRY_IN_5_MINUTES_PREFIX = "Vent 5 minutter";
    }

    public static class CardStatus {
        public static final String ACTIVE = "active";
    }

    public static class CreditCard {
        public static final String TRANSACTION_TYPE = "TransactionType";
        public static final String COHOLDER = "COHOLDER";
        public static final String MAINHOLDER = "MAINHOLDER";
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public static class RetryFilter {
        public static final int NUM_TIMEOUT_RETRIES = 10;
        public static final int RETRY_SLEEP_MILLISECONDS = 10000;
    }

    public static class Storage {
        public static final String OAUTH_TOKEN = "oauth_token";
        public static final String OAUTH_SECRET = "oauth_secret";
    }
}
