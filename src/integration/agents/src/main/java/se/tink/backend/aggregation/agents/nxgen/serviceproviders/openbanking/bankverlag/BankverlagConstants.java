package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n_aggregation.LocalizableParametrizedKey;

public final class BankverlagConstants {

    private BankverlagConstants() {
        throw new AssertionError();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorMessages {
        public static final String MISSING_SCA_METHOD_DETAILS = "Sca method details missing";
        public static final String STARTCODE_NOT_FOUND = "Startcode for Chip tan not found";
        public static final String NO_SUPPORTED_METHOD_FOUND = "No supported method found";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {
        public static final String BASE_URL = "https://psd2.bvxs2a.de/{aspspId}";
        static final URL CONSENT = new URL(BASE_URL + "/v1/consents");
        static final URL CONSENT_STATUS = new URL(CONSENT + "/{consentId}/status");
        static final URL CONSENT_DETAILS = new URL(CONSENT + "/{consentId}");
        static final URL FETCH_ACCOUNTS = new URL(BASE_URL + "/v1/accounts");
        static final URL FETCH_BALANCES = new URL(FETCH_ACCOUNTS + "/{accountId}/balances");
        static final URL FETCH_TRANSACTIONS = new URL(FETCH_ACCOUNTS + "/{accountId}/transactions");

        public static final URL CREATE_PAYMENT =
                new URL(BASE_URL + "/v1/{payment-service}/{payment-product}");
        public static final URL GET_PAYMENT =
                new URL(BASE_URL + "/v1/{payment-service}/{payment-product}/{paymentId}");
        public static final URL GET_PAYMENT_STATUS = new URL(GET_PAYMENT + "/status");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class PathVariables {
        static final String CONSENT_ID = "consentId";
        static final String ACCOUNT_ID = "accountId";
        static final String ASPSP_ID = "aspspId";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class QueryKeys {
        static final String DATE_FROM = "dateFrom";
        static final String BOOKING_STATUS = "bookingStatus";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class QueryValues {
        static final String BOOKED = "booked";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class HeaderKeys {
        static final String X_REQUEST_ID = "X-Request-ID";
        static final String CONSENT_ID = "Consent-ID";
        static final String PSU_ID = "PSU-ID";
        static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        static final String ASPSP_APPROACH = "Aspsp-Sca-Approach";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class FormValues {
        static final int FREQUENCY_PER_DAY = 4;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BankverlagAspspId {
        public static final String TARGOBANK = "targobank";
        public static final String DEGUSSABANK = "degussabank";
        public static final String CONSORSFINANZ = "consorsfinanz";
    }

    public static class HttpClient {
        public static final int MAX_RETRIES = 3;
        public static final int RETRY_SLEEP_MILLISECONDS = 3000;
    }

    public static class Fields {
        public static final LocalizableParametrizedKey INSTRUCTIONS =
                new LocalizableParametrizedKey(
                        "Please open the \"{0}\" Banking-App app and confirm the login. Then click the \"Submit\" button");
        public static final LocalizableParametrizedKey INSTRUCTIONS_WITH_APP_NAME =
                new LocalizableParametrizedKey(
                        "Please open the \"{0}\" Banking-App app and confirm the login via \"{1}\". Then click the \"Submit\" button");
    }
}
