package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas;

public final class BnpParibasBaseConstants {
    public static final String INTEGRATION_NAME = "bnpparibas";

    public static class Banks {
        public static final String BNPPARIBAS_HELLOBANK = "hellobank";
        public static final String BNPPARIBAS_MABANQUE = "retail";
    }

    public static class Urls {
        private static final String BASE_URL =
                "https://api-psd2.bddf.bnpparibas/psd2/{bank}/V1.4"; // "https://api-nav-psd2.sandbox.bddf.bnpparibas/psd2-sandbox/{bank}/V1.4";
        // "https://api-psd2.bddf.bnpparibas/psd2/retail/V1.4";
        public static final String AUTHENTICATION_URL =
                "https://api-nav-psd2.bddf.bnpparibas/as/psd2/{bank}/authorize"; // "https://api-nav-psd2.sandbox.bddf.bnpparibas/psd2-sandbox/{bank}/authorize";
        // "https://api-nav-psd2.bddf.bnpparibas/as/psd2/retail/authorize";
        public static final String TOKEN_URL =
                "https://api-psd2.bddf.bnpparibas/as/psd2/{bank}/token"; // "https://api-nav-psd2.sandbox.bddf.bnpparibas/psd2-sandbox/{bank}/token";
        //  "https://api-psd2.bddf.bnpparibas/as/psd2/retail/token";

        public static final String ACCOUNTS_PATH = BASE_URL + "/accounts";
        public static final String BALANCES_PATH = ACCOUNTS_PATH + "/{accountResourceId}/balances";
        public static final String TRANSACTIONS_PATH =
                ACCOUNTS_PATH + "/{accountResourceId}/transactions";
    }

    public class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String BEARER = "Bearer";
        public static final String REFRESH_TOKEN = "refresh_token";

        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public class QueryValues {
        public static final String CODE = "code";
        public static final String AISP = "aisp";
        public static final String FULL_SCOPES = "aisp extended_transaction_history";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String FORMATTER_MILLI_WITH_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
        public static final String TIMEZONE = "CET";
    }

    public class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Bnp Paribas configuration missing";
        public static final String MISSING_TOKEN = "Cannot find token";
    }

    public class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String SIGNATURE = "signature";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String X_REDIRECT_URI = "x-redirect";
    }

    public class HeaderValues {
        public static final String BASIC = "Basic ";
    }

    public class StorageKeys {
        public static final String TOKEN = "OAUTH_TOKEN";
    }

    public class IdTags {
        public static final String ACCOUNT_RESOURCE_ID = "accountResourceId";
        public static final String BANK = "bank";
    }
}
