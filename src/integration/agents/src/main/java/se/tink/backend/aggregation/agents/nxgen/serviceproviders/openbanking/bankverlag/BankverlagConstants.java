package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class BankverlagConstants {

    private BankverlagConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String MISSING_SCA_METHOD_DETAILS = "Sca method details missing";
        public static final String STARTCODE_NOT_FOUND = "Startcode for Chip tan not found";
        public static final String NO_SUPPORTED_METHOD_FOUND = "No supported method found";
    }

    public static class PsuErrorMessages {
        public static final String REQUEST_PROCESSING_ERROR =
                "Die Anfrage konnte nicht verarbeitet werden.";
    }

    public static class Urls {
        public static final String BASE_URL = "https://psd2.bvxs2a.de/{aspspId}";
        static final URL CONSENT = new URL(BASE_URL + "/v1/consents");
        static final URL CONSENT_STATUS = new URL(CONSENT + "/{consentId}/status");
        static final URL CONSENT_DETAILS = new URL(CONSENT + "/{consentId}");
        static final URL FETCH_ACCOUNTS = new URL(BASE_URL + "/v1/accounts");
        static final URL FETCH_BALANCES = new URL(FETCH_ACCOUNTS + "/{accountId}/balances");
        static final URL FETCH_TRANSACTIONS = new URL(FETCH_ACCOUNTS + "/{accountId}/transactions");
    }

    static class PathVariables {
        static final String CONSENT_ID = "consentId";
        static final String ACCOUNT_ID = "accountId";
        static final String ASPSP_ID = "aspspId";
    }

    static class QueryKeys {
        static final String DATE_FROM = "dateFrom";
        static final String BOOKING_STATUS = "bookingStatus";
    }

    static class QueryValues {
        static final String BOOKED = "booked";
    }

    static class HeaderKeys {
        static final String X_REQUEST_ID = "X-Request-ID";
        static final String CONSENT_ID = "Consent-ID";
        static final String PSU_ID = "PSU-ID";
        static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        static final String API_KEY = "X-bvpsd2-test-apikey";
    }

    static class HeaderValues {
        static final String API_KEY_VALUE = "tUfZ5KOHRTFrikZUsmSMUabKw09UIzGE";
    }

    static class FormValues {
        static final int FREQUENCY_PER_DAY = 4;
    }

    // ASPSP Id's under this list provide URL to download transactions as file, hence they need
    // special handling to parse transactions
    public static class AspspId {
        static final List<String> ASPSP_WITH_URI_FOR_TRANSACTIONS =
                Arrays.asList(BankverlagAspspId.TARGOBANK);
    }

    public static class BankverlagAspspId {
        public static final String TARGOBANK = "targobank";
        public static final String DEGUSSABANK = "degussabank";
        public static final String CONSORSFINANZ = "consorsfinanz";
    }

    public static class AuthMethods {
        private AuthMethods() {}

        public static final ImmutableList<String> UNSUPPORTED_AUTH_TYPES =
                ImmutableList.of("OPTICAL", "QR");
    }
}
