package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class SparkassenConstants {

    private SparkassenConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String MISSING_SCA_AUTHORIZATION_URL = "Sca Authorization Url missing";
        public static final String MISSING_SCA_METHOD_DETAILS = "Sca method details missing";
        public static final String COULD_NOT_PARSE_TRANSACTIONS =
                "Could not parse transactions description";
        public static final String COULD_NOT_INITIALIZE_JAXBCONTEXT =
                "Could not initialize JAXBContext";
        static final String PSU_CREDENTIALS_INVALID = "PSU_CREDENTIALS_INVALID";
        static final String IBAN_INVALID = "IBAN_INVALID";
        public static final String STARTCODE_NOT_FOUND = "Startcode fo Chip tan not found";
    }

    public static class Urls {
        public static final String BASE_URL = "https://xs2a.f-i-apim.de:8443/fixs2aop-env";
        public static final URL GET_CONSENT =
                new URL(BASE_URL + "/xs2a-api/{bankCode}/v1/consents");
        public static final URL UPDATE_SCA_METHOD =
                new URL(GET_CONSENT + "/{consentId}/authorisations/{authorizationId}");
        public static final URL FINALIZE_AUTHORIZATION =
                new URL(GET_CONSENT + "/{consentId}/authorisations/{authorizationId}");
        public static final URL CHECK_CONSENT_STATUS = new URL(GET_CONSENT + "/{consentId}/status");

        public static final URL FETCH_ACCOUNTS =
                new URL(BASE_URL + "/xs2a-api/{bankCode}/v1/accounts");
        public static final URL FETCH_BALANCES = new URL(FETCH_ACCOUNTS + "/{accountId}/balances");
        public static final URL FETCH_TRANSACTIONS =
                new URL(FETCH_ACCOUNTS + "/{accountId}/transactions");
    }

    public static class PathVariables {
        public static final String CONSENT_ID = "consentId";
        public static final String AUTHORIZATION_ID = "authorizationId";
        public static final String ACCOUNT_ID = "accountId";
        public static final String BANK_CODE = "bankCode";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
        public static final String PSU_ID = "PSU-ID";
    }

    public static class FormValues {
        public static final int FREQUENCY_PER_DAY = 4;
    }

    public static class AuthMethods {
        private AuthMethods() {}

        public static final ImmutableList<String> UNSUPPORTED_AUTH_TYPES =
                ImmutableList.of("OPTICAL", "QR");
    }
}
