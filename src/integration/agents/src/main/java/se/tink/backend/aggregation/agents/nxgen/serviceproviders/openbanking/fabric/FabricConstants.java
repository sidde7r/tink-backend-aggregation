package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public class FabricConstants {

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "CACC",
                            "CASH",
                            "CHAR",
                            "CISH",
                            "COMM",
                            "SLRY",
                            "TRAN",
                            "TRAS",
                            "CurrentAccount",
                            "Current")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "LLSV",
                            "ONDP",
                            "SVGS")
                    .build();

    private FabricConstants() {}

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MAPPING =
                "Cannot map payment status: %s to Tink payment status.";
    }

    public static class Urls {
        // test: https://test-psd2gateway.fabrick.com
        // sandbox: https://sandbox-psdgw-sella.fabrick.com
        public static final String BASE_URL = "https://psdgw-sella.fabrick.com";

        public static final String CONSENT = "/api/fabrick/psd2/v1/consents";
        public static final String GET_ACCOUNTS = "/api/fabrick/psd2/v1/accounts";
        public static final String GET_TRANSACTIONS =
                "/api/fabrick/psd2/v1/accounts/{accountId}/transactions";
        public static final String GET_CONSENT_STATUS =
                "/api/fabrick/psd2/v1/consents/{consentId}/status";
        public static final String GET_CONSENT_DETAILS =
                "/api/fabrick/psd2/v1/consents/{consentId}";
        public static final String API_PSD2_URL = "/api/fabrick/psd2";

        public static final String INITIATE_A_PAYMENT_URL =
                BASE_URL + "/api/fabrick/psd2/v1/{payment-service}/{payment-product}";
        public static final String PAYMENT_URL =
                BASE_URL + "/api/fabrick/psd2/v1/{payment-service}/{payment-product}/{paymentId}";
        public static final String GET_PAYMENT_STATUS_URL =
                BASE_URL
                        + "/api/fabrick/psd2/v1/{payment-service}/{payment-product}/{paymentId}/status";
        public static final String GET_PAYMENT_AUTHORIZATIONS_URL =
                BASE_URL
                        + "/api/fabrick/psd2/v1/{payment-service}/{payment-product}/{paymentId}/authorisations";
        public static final String GET_PAYMENT_AUTHORIZATION_STATUS_URL =
                BASE_URL
                        + "/api/fabrick/psd2/v1/{payment-service}/{payment-product}/{paymentId}/authorisations/{authorisationId}";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String CONSENT_ID = "consentId";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "CONSENT_ID";
        public static final String LINK = "link";
        public static final String PAYMENT_ID = "paymentId";
        public static final String PAYMENT_AUTHORIZATION_ID = "paymentAuthorizationId";
    }

    public static final class AuthenticationKeys {
        public static final String AUTHENTICATION_FINALISED = "finalised";
        public static final String AUTHENTICATION_PSU_AUTHENTICATED = "psuAuthenticated";
        public static final String SCA_METHOD_SELECTED = "scaMethodSelected";
        public static final String SMS_SCA = "SMS_OTP";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String STATE = "state";
        public static final String CODE = "code";
    }

    public static class QueryValues {
        public static final String CODE = "code";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String TPP_REDIRECT_PREFERED = "TPP-Redirect-Preferred";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_ID = "psu-id";
    }

    public static class HeaderValues {
        public static final String TPP_REDIRECT_PREFERED = "true";
    }

    public static class Accounts {
        public static final String CASH = "CASH";
    }

    public static class PathParameterKeys {
        public static final String PAYMENT_PRODUCT = "payment-product";
        public static final String PAYMENT_SERVICE = "payment-service";
        public static final String PAYMENT_ID = "paymentId";
        public static final String PAYMENT_TYPE = "paymentType";
        public static final String PAYMENT_AUTHORIZATION_ID = "authorisationId";
    }

    public static class PathParameterValues {
        public static final String PAYMENT_PRODUCT_SEPA_CREDIT = "sepa-credit-transfers";
        public static final String PAYMENT_PRODUCT_SEPA_INSTANT = "instant-sepa-credit-transfers";

        public static final String PAYMENT_SERVICE_PAYMENTS = "payments";
        public static final String PAYMENT_SERVICE_PERIODIC_PAYMENTS = "periodic-payments";
    }

    public static class Timer {
        public static final long WAITING_FOR_SUPPLEMENTAL_INFORMATION_MINUTES = 9L;
        public static final long WAITING_FOR_QUIT_PENDING_STATUS_MILISEC = 3000L;
    }

    public static class PaymentStep {
        public static final String IN_PROGRESS = "IN_PROGRESS";
    }
}
