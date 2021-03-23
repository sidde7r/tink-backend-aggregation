package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;

public class RabobankConstants {

    public static final String UUID_PATTERN =
            "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";
    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final String TRANSACTION_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TRANSACTION_BOOKING_DATE_FORMAT = "yyyy-MM-dd";
    public static final String BASE_URL = "https://api.rabobank.nl/openapi";
    public static final Pattern SPLITTERS = Pattern.compile(" E[NO] ", Pattern.CASE_INSENSITIVE);
    public static final String BALANCE_TYPE_EXPECTED = "expected";

    public static class ErrorCodes {
        public static final String PERIOD_INVALID = "PERIOD_INVALID";
        public static final String ACCESS_EXCEEDED = "ACCESS_EXCEEDED";
    }

    public static class ErrorMessages {

        public static final String BOOKING_STATUS_INVALID =
                "currently only bookingstatus booked is allowed";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final ImmutableList<Integer> ERROR_RESPONSES =
                ImmutableList.of(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        HttpStatus.SC_BAD_GATEWAY,
                        HttpStatus.SC_SERVICE_UNAVAILABLE);
        public static final String NOT_SUBSCRIBED = "Not registered to plan";
        public static final String ERROR_MESSAGE = "Error message: ";
    }

    public static class Consents {

        public static final String EXPIRE = "expire";
        public static final String INVALID = "invalid";
        public static final String REVOKED_BY_USER = "revoked";
    }

    public static class StorageKey {

        public static final String OAUTH_TOKEN =
                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String RESOURCE_ID = "resource_id";
        public static final String CONSENT_ID = "consentId";
        public static final String TOKEN_EXPIRY_DATE = "tokenExpiryDate";
    }

    public static class Signature {

        public static final String ALGORITHM = "algorithm";
        public static final String HEADERS = "headers";
        public static final String KEY_ID = "keyId";
        private static final String SHA_512 = "sha-512";
        public static final String RSA_SHA_256 = "rsa-sha256";
        public static final String SIGNING_STRING_DIGEST = "digest: ";
        public static final String SIGNING_STRING_DATE = "date: ";
        public static final String SIGNING_STRING_REQUEST_ID = "x-request-id: ";
        public static final String SIGNING_STRING_SHA_512 = SHA_512 + "=";
        public static final String SIGNATURE = "signature";
        public static final String HEADERS_VALUE = "date digest x-request-id";
    }

    public static class QueryParams {

        public static final String IBM_CLIENT_ID = "x-ibm-client-id";
        public static final String CLIENT_ID = "client_id";
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String DIGEST = "Digest";
        public static final String SIGNATURE = "Signature";
        public static final String DATE = "Date";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String SIZE = "size";
        public static final String PAGE = "page";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class QueryValues {

        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE = "code";
        public static final String BOOKED = "booked";
        public static final int TRANSACTIONS_SIZE = 500;
        public static final String SCOPES =
                "ais.balances.read ais.transactions.read-90days ais.transactions.read-history";
    }

    public static class HttpClient {

        public static final int MAX_RETRIES = 5;
        public static final int RETRY_SLEEP_MILLISECONDS = 2000;
    }
}
