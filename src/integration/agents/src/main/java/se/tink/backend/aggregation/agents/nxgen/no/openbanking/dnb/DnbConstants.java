package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import java.time.temporal.ChronoUnit;

public final class DnbConstants {
    private DnbConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String SCA_REDIRECT_LINK_MISSING = "ScaRedirect link is missing.";
        public static final String WRONG_BALANCE_TYPE =
                "Wrong balance type. Expected type not found.";

        static final String DNB_ERROR_WRONG_PSUID =
                "User id can be either an 11 digit valid Norwegian ssn - or 2 letters followed by 5 digits (business)";
    }

    public static class Urls {
        static final String BASE_URL = "https://api.psd.dnb.no";
        static final String CONSENTS = BASE_URL + "/v1/consents";
        static final String CONSENT_DETAILS = CONSENTS + "/{consentId}";
        static final String ACCOUNTS = BASE_URL + "/v1/accounts";
        static final String BALANCES = ACCOUNTS + "/{accountId}/balances";
        static final String TRANSACTIONS = ACCOUNTS + "/{accountId}/transactions";
        static final String PAYMENTS = BASE_URL + "/v1/payments/{paymentType}";
        static final String GET_PAYMENT = PAYMENTS + "/{paymentId}";
        static final String CARDS = BASE_URL + "/v1/card-accounts";
        static final String CARD_TRANSACTION = CARDS + "/{accountId}/transactions";
    }

    public static class HeaderKeys {
        public static final String PSU_ID = "PSU-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-Uri";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class CredentialsKeys {
        public static final String PSU_ID = "PSU-ID";
    }

    public static class QueryKeys {
        public static final String STATE = "state";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String FROM_DATE = "dateFrom";
        public static final String TO_DATE = "dateTo";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
    }

    public static class ConsentRequestValues {
        public static final int FREQUENCY_PER_DAY = 4;
        public static final int CONSENT_DAYS_VALID = 89;
        public static final boolean RECURRING = true;
        public static final boolean COMBINED_SERVICE = false;
    }

    public static class CreditCardFetcherValues {
        public static final int AT_A_TIME = 1;
        public static final ChronoUnit TIME_UNIT = ChronoUnit.MONTHS;
    }

    public static class IdTags {
        public static final String PAYMENT_TYPE = "paymentType";
        public static final String PAYMENT_ID = "paymentId";
        public static final String CONSENT_ID = "consentId";
        public static final String ACCOUNT_ID = "accountId";
    }
}
