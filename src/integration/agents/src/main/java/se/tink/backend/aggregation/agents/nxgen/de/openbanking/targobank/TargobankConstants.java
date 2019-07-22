package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class TargobankConstants {

    public static final String INTEGRATION_NAME = "targobank";

    private TargobankConstants() {
        throw new AssertionError();
    }

    // TODO: No info about account types
    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "CACC")
                    .setDefaultTranslationValue(TransactionalAccountType.OTHER)
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String SCA_NOT_FOUND = "No available sca methods";
        public static final String MAPPING =
                "Cannot map payment status: %s to Tink payment status.";
    }

    private static class SandboxEndpoints {
        private static String BASE = "https://www.sandbox-bvxs2a.de/targobank/v1";
        private static String CREATE_CONSENT = BASE + "/consents";
        private static String FETCH_ACCOUNTS = BASE + "/accounts";
    }

    public static class SandboxUrls {
        public static URL CREATE_CONSENT = new URL(SandboxEndpoints.CREATE_CONSENT);
        public static URL FETCH_ACCOUNTS = new URL(SandboxEndpoints.FETCH_ACCOUNTS);
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class PathVariable {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class QueryKeys {
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String WITH_BALANCE = "true";
        public static final String BOOKING_STATUS = "booked";
    }

    public static class HeaderKeys {
        public static String PSU_ID = "PSU-ID";
        public static String PSU_IP_Address = "PSU-IP-Address";
        public static String X_Request_ID = "X-Request-ID";
        public static String API_KEY = "X-bvpsd2-test-apikey";
        public static String TPP_Redirect_URI = "TPP-Redirect-URI";
        public static String CONSENT_ID = "Consent-ID";
    }

    public static class HeaderValues {
        public static String PSU_ID = "PSD2TEST2";
    }

    public static class ScaStatuses {
        public static final String FINALISED = "finalised";
    }
}
