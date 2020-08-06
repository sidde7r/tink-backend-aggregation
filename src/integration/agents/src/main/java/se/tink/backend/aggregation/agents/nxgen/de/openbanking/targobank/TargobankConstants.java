package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;

public final class TargobankConstants {

    public static final String INTEGRATION_NAME = "targobank";

    private TargobankConstants() {
        throw new AssertionError();
    }

    // TODO: No info about account types
    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "CACC")
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
        private static final String BASE = "https://www.sandbox-bvxs2a.de/targobank/v1";
        private static final String CREATE_CONSENT = BASE + "/consents";
        private static final String FETCH_ACCOUNTS = BASE + "/accounts";
        private static final String CREATE_PAYMENT = BASE + "/payments/sepa-credit-transfers/";
        private static final String FETCH_PAYMENT =
                BASE + "/payments/sepa-credit-transfers/{paymentId}";
    }

    public static class SandboxUrls {
        public static final URL CREATE_CONSENT = new URL(SandboxEndpoints.CREATE_CONSENT);
        public static final URL FETCH_ACCOUNTS = new URL(SandboxEndpoints.FETCH_ACCOUNTS);
        public static final URL CREATE_PAYMENT = new URL(SandboxEndpoints.CREATE_PAYMENT);
        public static final URL FETCH_PAYMENT = new URL(SandboxEndpoints.FETCH_PAYMENT);
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class PathVariable {
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class QueryKeys {
        public static final String WITH_BALANCE = "withBalance";
    }

    public static class QueryValues {
        public static final String WITH_BALANCE = "true";
    }

    public static class HeaderKeys {
        public static final String PSU_ID = "PSU-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String API_KEY = "X-bvpsd2-test-apikey";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class HeaderValues {
        public static final String PSU_ID = "PSD2TEST2";
    }

    public static class ScaStatuses {
        public static final String FINALISED = "finalised";
    }
}
