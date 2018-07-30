package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class ErsteBankConstants {

    public static String LOCATION = "Location";

    public static class ENCRYPTION {
        public static String RSA = "RSA";
        public static String HEX_DIGITS = "0123456789ABCDEF";
    }

    public static class URLS {
       public static final String LOGIN_BASE = "https://login.sparkasse.at";
       public static final String OAUTH = "/sts/oauth/authorize";
       public static final String GEORGE_GO_BASE = "https://georgego.sparkasse.at";
       public static final String ACCOUNT = "/bff/b/overview";
       public static final String SPARKASSE_BASE = "https://api.sparkasse.at";
       public static final String LOGOUT = "/rest/netbanking/auth/token/invalidate";
    }

    public static class HEADERS {
        public static String ACCEPT = "*/*";
        public static String X_MOBILE_APP_ID = "x-mobile-app-id";
        public static String X_MOBILE_APP_ID_IOS = "georgego-ios";
        public static String ENVIROMENT = "Environment";
        public static String ENVIROMENT_PROD = "AUSTRIA_PROD";
        public static String X_APP_ID = "X-App-Id";
        public static String X_APP_ID_TRANSACTIONAPP = "transactionapp";
        public static String X_REQUEST_ID = "X-REQUEST-ID";
        public static String AUTHORIZATION = "Authorization";
        public static String BEARER = "Bearer ";
    }

    public static class QUERYPARAMS {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String RESPONSE_TYPE_TOKEN = "token";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_ID_TRANSACTIONAPP = "transactionapp";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REDIRECT_URI_AUTHENTICATION = "transactionapp://authentication";
        public static final String SPARKASSE_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        public static final String FEATURES = "features";
        public static final String FEATURES_ORDERS = "qr,orders";
        public static final String PAGE = "page";
    }

    public static class STORAGE {
        public static String TOKEN_ENTITY = "TOKEN_ENTITY";
        public static String TRANSACTIONSURL = "ACCOUNT_URL";
    }

    public static class PATTERN {
        public static final Pattern SALT = Pattern.compile("\"saltCode\"\\s+value=\"(.+?)\".*", Pattern.DOTALL | Pattern.MULTILINE);
        public static final Pattern MODULUS = Pattern.compile("\"modulus\"\\s+value=\"(.+?)\"", Pattern.DOTALL | Pattern.MULTILINE);
        public static final Pattern EXPONENT = Pattern.compile("\"exponent\"\\s+value=\"(.+?)\"", Pattern.DOTALL | Pattern.MULTILINE);

        public static final Pattern ACCESS_TOKEN = Pattern.compile("access_token=(.*)&token_type");
        public static final Pattern TOKEN_TYPE = Pattern.compile("token_type=(.*)&expires_in");
        public static final Pattern EXPIRES_IN = Pattern.compile("expires_in=(.*)&scope");

        public static String DATE_FORMAT = "M/d/y";
        public static final String TRANSACTION_FORMAT = "/bff/b/products/%s/transactions";
    }

    public static class BODY {
        public static String USERNAME = "j_username=";
        public static String JAVASCRIPT_ENABLED = "javaScript=jsOK";
        public static String RSA_ENCRYPTED = "rsaEncrypted=";
    }

    public static class LOGTAG {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("ERSTEBANK_SPARKASSE_ACCOUNT_TYPE");
    }
}
