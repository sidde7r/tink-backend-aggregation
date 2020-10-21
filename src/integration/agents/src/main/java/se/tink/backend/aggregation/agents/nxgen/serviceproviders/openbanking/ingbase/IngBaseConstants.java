package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import com.google.common.collect.ImmutableList;
import org.apache.http.HttpStatus;

public final class IngBaseConstants {

    private IngBaseConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String BASE_URL = "https://api.ing.com";
        public static final String ACCOUNTS = "/v3/accounts";
        private static final String BASE_AUTH = "/oauth2";
        public static final String OAUTH = BASE_AUTH + "/authorization-server-url";
        public static final String TOKEN = BASE_AUTH + "/token";
    }

    public static class Transaction {
        // Time after authentication when full transaction history can be fetched (milliseconds).
        // Documentation says "immediately" after authentication. In practice, this is 1h.
        public static final long FULL_HISTORY_MAX_AGE = 30 * 60 * 1000L;
        public static final long DEFAULT_HISTORY_DAYS = 89;
    }

    public static class StorageKeys {
        public static final String AUTHENTICATION_TIME = "AUTHENTICATION_TIME";
        public static final String CLIENT_ID = "CLIENT_ID";
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
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String LIMIT = "limit";
    }

    public static class QueryValues {
        public static final String CODE = "code";
        public static final String PAYMENT_ACCOUNTS_TRANSACTIONS_AND_BALANCES_VIEW =
                "payment-accounts:transactions:view payment-accounts:balances:view";
        public static final String TRANSACTIONS_LIMIT = "50";
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
        public static final String UNKNOWN_ERROR = "Error unknown";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find Token!";
        public static final String MISSING_CLIENT_ID = "Cannot find client id!";
        public static final String INVALID_GRANT_ERROR = "invalid_grant";
        public static final ImmutableList<Integer> ERROR_CODES =
                ImmutableList.of(HttpStatus.SC_NOT_FOUND, HttpStatus.SC_BAD_GATEWAY);
    }

    public static class ErrorCodes {
        public static final String NOT_FOUND = "NOT_FOUND";
    }

    static class HttpClient {
        public static final int MAX_ATTEMPTS = 5;
        public static final int RETRY_SLEEP_MILLISECONDS = 2000;
    }
}
