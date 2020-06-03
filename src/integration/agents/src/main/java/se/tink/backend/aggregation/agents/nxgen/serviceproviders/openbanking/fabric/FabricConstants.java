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
                "/api/fabrick/psd2/v1/payments/{payment-product}";
        public static final String CREATE_PIS_AUTHORIZATION_URL =
                "/api/fabrick/psd2/v1/{paymentType}/{paymentProduct}/{paymentId}/authorisations";
        public static final String GET_PAYMENT_URL =
                "/api/fabrick/psd2/v1/payments/{payment-product}/{paymentId}";
        public static final String GET_PAYMENT_STATUS_URL =
                "/api/fabrick/psd2/v1/payments/{payment-product}/{paymentId}/status";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String CONSENT_ID = "consentId";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "CONSENT_ID";
        public static final String LINK = "link";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String STATE = "state";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String TPP_REDIRECT_PREFERED = "TPP-Redirect-Preferred";
    }

    public static class HeaderValues {
        public static final String TPP_REDIRECT_PREFERED = "true";
    }

    public static class Consent {
        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final String FREQUENCY_PER_DAY = "4";
        public static final String VALID_UNTIL = "9999-12-31";
        public static final String VALID = "valid";
    }

    public static class Accounts {
        public static final String BALANCE_CLOSING_BOOKED = "closingBooked";
        public static final String OTHER = "OTHER";
    }

    public static class PathParameterKeys {
        public static final String PAYMENT_PRODUCT = "payment-product";
        public static final String PAYMENT_ID = "paymentId";
        public static final String PAYMENT_TYPE = "paymentType";
    }

    public static class PathParameterValues {
        public static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
        public static final String PAYMENT_TYPE = "payments";
    }

    public static class Timer {
        public static final long WAITING_FOR_SUPPLEMENTAL_INFORMATION_TIMER = 9l;
    }

    public static class ScaStatus {
        public static final String SCA_FAILED = "failed";
        public static final String IDENTIFICATION_REQUIRED = "psuIdentified";
        public static final String AUTHENTICATION_REQUIRED = "psuAuthenticated";
        public static final String SCA_FINALISED = "finalised";
        public static final String EXEMPTED = "exempted";
    }

    public static class PaymentStep {
        public static final String IN_PROGRESS = "IN_PROGRESS";
    }
}
