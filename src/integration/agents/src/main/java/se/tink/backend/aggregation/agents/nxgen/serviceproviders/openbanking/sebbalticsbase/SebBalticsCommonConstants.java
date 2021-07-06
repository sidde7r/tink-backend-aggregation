package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase;

import java.net.InetAddress;

public class SebBalticsCommonConstants {

    public static final String DUMMY_IP = "0.0.0.0";

    public static String getPsuIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return SebBalticsCommonConstants.DUMMY_IP;
        }
    }

    public static class Urls {
        public static final String BASE_AUTH_URL = "https://api.ob.baltics.sebgroup.com";
        public static final String BASE_URL = "https://api.ob.baltics.sebgroup.com";
        public static final String NEW_CONSENT = BASE_URL + "/v2/consents";
        public static final String CONSENT_AUTHORIZATION =
                BASE_URL + "/v2/consents/{consentId}/authorizations";
        public static final String CONSENT_STATUS = BASE_URL + "/v2/consents/{consentId}/status";
        public static final String DECOUPLED_AUTHORIZATION =
                BASE_URL + "/v2/oauth/authorize-decoupled";
        public static final String DECOUPLED_TOKEN = BASE_URL + "/v2/oauth/token";
        public static final String ACCOUNTS = "/v2/accounts";
        public static final String ACCOUNTS_LIST = BASE_URL + "/v2/account-list";
        public static final String TRANSACTIONS =
                BASE_URL + "/v2/accounts/{accountId}/transactions";
        public static final String BALANCES = BASE_URL + "/v2/accounts/{accountId}/balances";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String DATE = "Date";
        public static final String CLIENT_ID = "client-id";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String PSU_INVOLVED = "PSU-involved";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_Id = "PSU-ID";
        public static final String PSU_CORPORATE_ID = "PSU-Corporate-ID";
    }

    public static class StorageKeys {
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class Accounts {
        public static final String AVAILABLE_BALANCE = "interimAvailable";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String SEB_SPECIFIC_ERROR = "SEB specific error has occurred.";
    }

    public static class QueryKeys {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String SCOPE = "accounts account.lists consents";
        public static final int FREQUENCY_PER_DAY = 4;
        public static final String PENDING_AND_BOOKED_TRANSACTIONS = "both";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String CONSENT_ID = "consentId";
    }

    public static class AccountTypes {
        public static final String CURRENT = "currentaccount";
    }

    public static class TransactionType {
        public static final String UPCOMING = "upcoming";
        public static final String RESERVED = "reserved";
    }
}
