package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class LhvConstants {

    public static class Url {
        public static final String BASE_URL = "https://api.lhv.eu/psd2/v1";
        public static final URL AUTH = new URL(BASE_URL + "/oauth/authorisations");
        public static final URL AUTH_STATUS = new URL(AUTH + "/{authorisationId}");

        public static final URL GET_OAUTH2_TOKEN = new URL("https://api.lhv.eu/psd2/oauth/token");

        public static final URL ACCOUNT_SUMMARY_LIST = new URL(BASE_URL + "/accounts-list");
        public static final URL CONSENT = new URL(BASE_URL + "/consents");
        public static final URL ACCOUNT_LIST = new URL(BASE_URL + "/accounts");
        public static final URL BALANCE = new URL(BASE_URL + "/accounts/{resourceId}/balances");
        public static final URL CONSENT_STATUS = new URL(BASE_URL + "/consents/{consentId}/status");
        public static final URL TRANSACTIONS =
                new URL(BASE_URL + "/accounts/{resourceId}/transactions");
    }

    public static class IdTags {
        public static final String AUTHORIZATION_ID = "authorisationId";
        public static final String CONSENT_ID = "consentId";
        public static final String RESOURCE_ID = "resourceId";
        public static final String DATE_FROM = "dateFrom";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class AuthenticationParams {

        public static final String DEVICE_NAME = "Tink";
    }

    public static class AccountTypes {
        public static final String CURRENT = "cacc";
    }

    public static class QueryKey {
        public static final String PSU_CORPORATE_ID = "PSU-Corporate-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
    }

    public static class QueryValues {

        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final long FREQUENCY_PER_DAY = 4;
        public static final boolean RECURRING_INDICATOR = true;
        public static final boolean COMBINED_SERVICE_INDICATOR = true;
        public static final String NULL = "null";
        public static final String BOOKING_STATUS = "booked";
    }

    public static class StorageKeys {
        public static final String USER_CONSENT_ID = "ConsentId";
        public static final String AUTHORISATION_ID = "AuthorisationId";
        public static final String OAUTH_2_TOKEN = "OAuth2Token";
        public static final String AUTHORISATION_CODE = "authorisationCode";
    }

    public static class ErrorMessages {

        public static final String TOKEN_NOT_FOUND = "Access token not found in storage.";
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
    }

    public static class ConsentStatus {
        public static final String VALID = "valid";
        public static final String RECEIVED = "received";
        public static final String REJECTED = "rejected";
        public static final String EXPIRED = "expired";
        public static final String REVOKED = "revokedByPsu";
        public static final String TERMINATED = "terminatedByTpp";
    }

    public static class GrantType {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class AuthenticationMethod {
        public static final String SMART_ID = "SID";
    }

    public static class PollValues {
        public static final int SMART_ID_POLL_MAX_ATTEMPTS = 90;
        public static final int SMART_ID_POLL_FREQUENCY = 2000;
    }

    public static class AuthorisationStatus {
        public static final String STARTED = "STARTED";
        public static final String FAILED = "FAILED";
        public static final String FINALISED = "FINALISED";
        public static final String SCA_METHOD_SELECTED = "SCA_METHOD_SELECTED";
        public static final String RECEIVED = "RECEIVED";
    }

    public static class TransactionsTags {
        public static final String PAYMENT = "Payment";
    }

    public static class Accounts {
        public static final String AVAILABLE_BALANCE = "interimAvailable";
    }
}
