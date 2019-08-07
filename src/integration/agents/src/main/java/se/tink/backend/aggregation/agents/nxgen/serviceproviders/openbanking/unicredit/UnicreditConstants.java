package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;

public final class UnicreditConstants {

    private UnicreditConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_CREDENTIALS = "Client credentials missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String MISSING_SCA_URL = "scaRedirect url not present in response";
        public static final String ACCOUNT_BALANCE_NOT_FOUND = "Account balance not found";
        public static final String UNDEFINED_BALANCE_TYPE =
                "There is balanceType of value '%s' defined in Enum %s";
    }

    public static class Endpoints {

        public static final String CONSENTS = "/v1/consents";
        public static final String UPDATE_CONSENT = "/v1/consents/{consent-id}";
        public static final String CONSENT_STATUS = "/v1/consents/{consent-id}/status";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String BALANCES = "/v1/accounts/{account-id}/balances";
        public static final String TRANSACTIONS = "/v1/accounts/{account-id}/transactions";
    }

    public static class PathParameters {

        public static final String CONSENT_ID = "consent-id";
        public static final String ACCOUNT_ID = "account-id";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "CONSENT_ID";
    }

    public static class QueryKeys {

        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
        public static final String TRANSACTION_FROM_DATE = "1970-01-01";
        public static final int MAX_PERIOD_IN_DAYS = 89;
    }

    public static class HeaderKeys {

        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String PSU_ID_TYPE = "PSU-ID-Type";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String TPP_REDIRECT_PREFERED = "TPP-Redirect-Preferred";
        public static final String PSU_ID = "PSU-ID";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class HeaderValues {

        public static final String CODE = "code";
        public static final String PSU_IP_ADDRESS = "0.0.0.0";
    }

    public static class FormValues {

        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final int FREQUENCY_PER_DAY = 4;
    }

    public class Formats {
        public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    }

    public static class ConsentStatusStates {

        public static final String VALID = "valid";
    }
}
