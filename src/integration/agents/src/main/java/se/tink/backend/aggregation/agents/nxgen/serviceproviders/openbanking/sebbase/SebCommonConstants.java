package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import java.net.InetAddress;
import java.time.ZoneId;

public class SebCommonConstants {

    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Stockholm");
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DUMMY_IP = "0.0.0.0";

    public static String getPsuIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return SebCommonConstants.DUMMY_IP;
        }
    }

    public static class Urls {
        public static final String BASE_AUTH_URL = "https://authorize.sebgroup.com";
        public static final String BASE_URL = "https://tpp-api.sebgroup.com";

        public static final String DECOUPLED_AUTHORIZATION =
                BASE_AUTH_URL + "/auth/v3/authorizations";
        public static final String DECOUPLED_TOKEN = BASE_AUTH_URL + "/auth/v3/tokens";
        public static final String INIT_BANKID = BASE_AUTH_URL + "/auth/bid/v2/authentications";
        private static final String BASE_OAUTH = BASE_AUTH_URL + "/mga/sps/oauth";
        public static final String OAUTH = BASE_OAUTH + "/oauth20/dpsd2/authorize";
        public static final String TOKEN = BASE_URL + "/mga/sps/oauth/oauth20/dpsd2/token";
        public static final String OAUTH2_TOKEN = BASE_URL + "/mga4/sps/oauth/oauth20/token";
    }

    public static class PollResponses {
        public static final String COMPLETE = "complete";
        public static final String PENDING = "pending";
        public static final String FAILED = "failed";
        public static final String NO_CLIENT = "no_client";
        public static final String USER_CANCEL = "user_cancel";
        public static final String CANCELLED = "cancelled";
        public static final String TIMEOUT = "timeout";
        public static final String REQUIRES_EXTRA_VERIFICATION =
                "bankid_requires_extra_verification";
    }

    public static class HintCodes {
        public static final String OUTSTANDING_TRANSACTION = "OUTSTANDING_TRANSACTION";
        public static final String USER_SIGN = "USER_SIGN";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CONSENT_FORM_VERIFIER = "consent_form_verifier";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String GRANT_TYPE = "grant_type";
        public static final String TRUST_LEVEL = "trust_level";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE_TOKEN = "code";
        public static final String SCOPE = "psd2_accounts psd2_payments";
        public static final String AUTH_CODE_GRANT = "authorization_code";
        public static final String REFRESH_TOKEN_GRANT = "refresh_token";
        public static final String BOOKED_TRANSACTIONS = "booked";
        public static final String WITH_BALANCE = "true";
        public static final String PENDING_AND_BOOKED_TRANSACTIONS = "both";
        public static final String PERMIT = "permit";
        public static final String PENDING = "pending";
    }

    public static class BodyValues {
        public static final String AST = "ast";
        public static final String QR = "qr";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_CORPORATE_ID = "PSU-CORPORATE-ID";
    }

    public static class StorageKeys {
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class Accounts {
        public static final String AVAILABLE_BALANCE = "interimAvailable";
        public static final String AVAILABLE_BALANCE_MISSPELLED = "interimAvaliable";
        public static final String AVAILABLE_BALANCE_EXPECTED = "expected";
        public static final String STATUS_ENABLED = "enabled";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class AccountTypes {
        public static final String SAVINGS = "sparkonto";
    }

    public static class TransactionType {
        public static final String UPCOMING = "upcoming";
        public static final String RESERVED = "reserved";
    }

    public static class ErrorMessages {
        public static final String PAGINATING_ERROR_CODE = "301";
        public static final String PAGINATING_ERROR_MESSAGE =
                "Unhandeled HostRC code: 2000 Error message is available.";
        public static final String INVALID_GRANT_ERROR = "invalid_grant";
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String SEB_SPECIFIC_ERROR = "SEB specific error has occurred.";
        public static final String REQUIRES_EXTRA_VERIFICATION =
                "Message from SEB - SEB needs you to verify your BankID before you can continue using the service. Visit www.seb.se or open the SEB app to verify your BankID. Note that you must be a customer of SEB to be able to use the service.";
    }

    public static class HttpClient {
        public static final int MAX_RETRIES = 5;
        public static final int RETRY_SLEEP_MILLISECONDS = 2000;
        public static final int NO_RESPONSE_MAX_RETRIES = 3;
        public static final int NO_RESPONSE_SLEEP_MILLISECONDS = 1000;
    }
}
