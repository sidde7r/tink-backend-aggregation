package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing;

public final class IngConstants {
  
    public static final String INTEGRATION_NAME = "ing";

    private IngConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String ACCOUNTS = "/v2/accounts";
        private static final String BASE_AUTH = "/oauth2";
        public static final String OAUTH = BASE_AUTH + "/authorization-server-url";
        public static final String TOKEN = BASE_AUTH + "/token";
    }

    public static class StorageKeys {
        public static final String BASE_URL = "BASE_URL";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String CLIENT_SIGNING_KEY_PATH = "CLIENT_SIGNING_KEY_PATH";
        public static final String CLIENT_SIGNING_CERTIFICATE_PATH =
                "CLIENT_SIGNING_CERTIFICATE_PATH";
        public static final String REDIRECT_URL = "REDIRECT_URL";
        public static final String TOKEN = "TOKEN";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String APPLICATION_TOKEN = "APPLICATION_TOKEN";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
    }

    public static class QueryKeys {
        public static final String SCOPE = "scope";
        public static final String COUNTRY_CODE = "country_code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String DATE_FORMAT = "yyyy-MM-dd";
    }

    public static class HeaderKeys {
        public static final String DIGEST = "Digest";
        public static final String DATE = "Date";
        public static final String X_ING_REQUEST_ID = "X-ING-ReqID";
        public static final String AUTHORIZATION = "Authorization";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String SIGNATURE = "Signature";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String EMPTY = "";
    }

    public static class Signature {
        public static final String SIGNING_STRING = "(request-target): ";
        public static final String DATE = "date: ";
        public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
        public static final String TIMEZONE = "GMT";
        public static final String DIGEST = "digest: ";
        public static final String X_ING_REQUEST_ID = "x-ing-reqid: ";
        public static final String ALGORITHM = "algorithm=\"rsa-sha256\"";
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String SIGNATURE = "Signature";
        public static final String KEY_ID_NAME = "keyId=";
        public static final String SIGNING_ALGORITHM = "RSA";
        public static final String HEADERS = "headers=\"(request-target) date digest x-ing-reqid\"";
        public static final String SIGNATURE_NAME = "signature=";
        public static final String HTTP_METHOD_POST = "post";
        public static final String HTTP_METHOD_GET = "get";
    }

    public static class BalanceTypes {
        public static final String EXPECTED = "expected";
        public static final String INTERIM_BOOKED = "interimBooked";
        public static final String CLOSING_BOOKED = "closingBooked";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }
}
