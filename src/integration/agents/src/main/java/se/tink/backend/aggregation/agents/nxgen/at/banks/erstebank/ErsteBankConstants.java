package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class ErsteBankConstants {

    public static String LOCATION = "Location";

    public static class ACCOUNTYPE {
        public static final String CHECKING = "GIRO";
        public static final String SAVING = "SAVING";
        public static final String BUILDING_SAVING = "BUILDING_SAVING";
        public static final String CARD_CREDIT = "CARD_CREDIT";
    }

    public static class ENCRYPTION {
        public static final String RSA = "RSA";
        public static final String HEX_DIGITS = "0123456789ABCDEF";
    }

    public static class URLS {
        public static final String LOGIN_BASE = "https://login.sparkasse.at";
        public static final String OAUTH = "/sts/oauth/authorize";
        public static final String GEORGE_GO_BASE = "https://georgego.sparkasse.at";
        public static final String ACCOUNT = "/bff/b/overview";
        public static final String SPARKASSE_BASE = "https://api.sparkasse.at";
        public static final String LOGOUT = "/rest/netbanking/auth/token/invalidate";
        public static final String POLL = "/sts/secapp/secondfactor";
    }

    public static class HEADERS {
        public static final String ACCEPT = "*/*";
        public static final String X_MOBILE_APP_ID = "x-mobile-app-id";
        public static final String X_MOBILE_APP_ID_IOS = "georgego-ios";
        public static final String ENVIROMENT = "Environment";
        public static final String ENVIROMENT_PROD = "AUSTRIA_PROD";
        public static final String X_APP_ID = "X-App-Id";
        public static final String X_APP_ID_TRANSACTIONAPP = "transactionapp";
        public static final String X_REQUEST_ID = "X-REQUEST-ID";
        public static final String AUTHORIZATION = "Authorization";
        public static final String BEARER = "Bearer ";
    }

    public static class QUERYPARAMS {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String RESPONSE_TYPE_TOKEN = "token";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_ID_TRANSACTIONAPP = "transactionapp";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REDIRECT_URI_AUTHENTICATION = "transactionapp://authentication";
        public static final String SPARKASSE_ACCEPT =
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        public static final String FEATURES = "features";
        public static final String FEATURES_ALL =
                "qr,orders,atinsurances,skmobiletopup,roccrepayment,cardpermanentlock";
        public static final String PAGE = "page";
    }

    public static class STORAGE {
        public static final String TOKEN_ENTITY = "TOKEN_ENTITY";
        public static final String TRANSACTIONSURL = "ACCOUNT_URL";
        public static final String CREDITURL = "CREDIT_URL";
        public static final String USERNAME = "USERNAME";
    }

    public static class PATTERN {
        public static final Pattern SALT =
                Pattern.compile(
                        "\"saltCode\"\\s+value=\"(.+?)\".*", Pattern.DOTALL | Pattern.MULTILINE);
        public static final Pattern MODULUS =
                Pattern.compile(
                        "\"modulus\"\\s+value=\"(.+?)\"", Pattern.DOTALL | Pattern.MULTILINE);
        public static final Pattern EXPONENT =
                Pattern.compile(
                        "\"exponent\"\\s+value=\"(.+?)\"", Pattern.DOTALL | Pattern.MULTILINE);

        public static final Pattern ACCESS_TOKEN = Pattern.compile("access_token=(.*)&token_type");
        public static final Pattern TOKEN_TYPE = Pattern.compile("token_type=(.*)&expires_in");
        public static final Pattern EXPIRES_IN = Pattern.compile("expires_in=(.*)&scope");
        public static final Pattern SIDENTITY_VERIFICATION_CODE =
                Pattern.compile("Verification code: <b>(.*)<\\/b>");

        public static final String DATE_FORMAT = "M/d/y";
        public static final String TRANSACTION_FORMAT = "/bff/b/products/%s/transactions";
    }

    public static class BODY {
        public static final String USERNAME = "j_username=";
        public static final String JAVASCRIPT_ENABLED = "javaScript=jsOK";
        public static final String RSA_ENCRYPTED = "rsaEncrypted";
        public static final String AUTHENTICATION_METHOD = "authenticationMethod";
        public static final String AUTHENTICATION_METHOD_PASSWORD = "PASSWORD";
    }

    public static class LOGTAG {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE =
                LogTag.from("ERSTEBANK_SPARKASSE_ACCOUNT_TYPE");
        public static final LogTag ERROR_DATE_PARSING = LogTag.from("ERSTEBANK_ERROR_PARSING_DATE");
        public static final LogTag CREDIT_TRANSACTIONS_ERROR =
                LogTag.from("ERSTEBANK_TRANSACTIONS_ERROR");
        public static final LogTag TRANSANSACTIONAL_ACC_ERR =
                LogTag.from("ERSTEBANK_TRANSANSACTIONAL_ACC_ERR");
        public static final LogTag CREDIT_ACC_ERR = LogTag.from("ERSTEBANK_CREDIT_ACC_ERR");
    }

    public static class DATE {
        public static final String YESTERDAY = "Yesterday";
        public static final String TODAY = "Today";
        public static final String TOMORROW = "Tomorrow";
    }

    public static class SIDENTITY {
        public static final int MAX_SIDENTITY_POLLING_ATTEMPTS = 80;
        public static final String POLL_WAITING = "PENDING";
        public static final String POLL_DONE = "DONE";
    }
}
