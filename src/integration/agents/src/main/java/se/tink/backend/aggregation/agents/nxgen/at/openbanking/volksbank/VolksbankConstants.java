package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class VolksbankConstants {

    public static final String INTEGRATION_NAME = "volksbank";

    private VolksbankConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_SCA_URL = "Missing sca redirect url.";
        public static final String MISSING_BALANCE = "No balance found.";
        public static final String INVALID_CONSENT = "Invalid consent!";
    }

    public static class Urls {

        private static final String BASE_URL = "https://www.banking.co.at/xs2a-sandbox/m044/v1";

        public static final URL CONSENTS = new URL(BASE_URL + Endpoints.CONSENTS);
        public static final URL CONSENT_STATUS = new URL(BASE_URL + Endpoints.CONSENT_STATUS);
        public static final URL ACCOUNTS = new URL(BASE_URL + Endpoints.ACCOUNTS);
        public static final URL BALANCES = new URL(BASE_URL + Endpoints.BALANCES);
        public static final URL TRANSACTIONS = new URL(BASE_URL + Endpoints.TRANSACTIONS);
    }

    private static class Endpoints {

        public static final String CONSENTS = "/consents";
        public static final String CONSENT_STATUS = "/consents/{consent-id}/status";
        public static final String ACCOUNTS = "/accounts";
        public static final String BALANCES = "/accounts/{account-id}/balances";
        public static final String TRANSACTIONS = "/accounts/{account-id}/transactions";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "CONSENT_ID";
        public static final String CACHED_ACCOUNTS = "CACHED_ACCOUNTS";
    }

    public static class QueryKeys {

        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String STATE = "state";
        public static final String CODE = "code";
    }

    public static class QueryValues {

        public static final String BOTH = "both";
        public static final String CODE = "code";
    }

    public static class HeaderKeys {

        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String PSU_ID = "PSU-ID";
        public static final String PSU_ID_TYPE = "PSU-ID-TYPE";
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class UrlParameters {

        public static final String CONSENT_ID = "consent-id";
        public static final String ACCOUNT_ID = "account-id";
    }

    public static class Balance {

        public static final String INTERIM_AVAILABLE = "interimAvailable";
    }

    public static class Formats {

        public static final String DATE_FORMAT = "yyyy-MM-dd";
    }

    public static class FormValues {

        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final String MAX_DATE = "9999-12-31";
    }

    public static class Status {

        public static final String VALID = "valid";
    }
}
