package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public final class SparebankConstants {

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            "Current",
                            "Current",
                            "Commission",
                            "TransactingAccount")
                    .put(TransactionalAccountType.SAVINGS, "Savings")
                    .setDefaultTranslationValue(TransactionalAccountType.OTHER)
                    .build();

    private SparebankConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String FETCH_TRANSACTIONS = "/v1/accounts/{resourceId}/transactions";
        public static final String GET_ACCOUNTS = "/v1/accounts";
        public static final String GET_SCA_REDIRECT = GET_ACCOUNTS;
        public static final String CREATE_PAYMENT = "/v1/payments/{paymentProduct}";
        public static final String GET_PAYMENT = "/v1/payments/{paymentProduct}/{paymentId}";
        public static final String SIGN_PAYMENT =
                "/v1/payments/{paymentProduct}/{paymentId}/authorisations";
        public static final String GET_PAYMENT_STATUS =
                "/v1/payments/{paymentProduct}/{paymentId}/status";
    }

    public static class QueryParams {
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String TPP_ID = "tpp-id";
        public static final String TPP_SESSION_ID = "tpp-session-id";
        public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
        public static final String TPP_SIGNATURE_CERTIFICATE = "tpp-signature-certificate";
        public static final String SIGNATURE = "signature";
        public static final String PSU_IP_ADDRESS = "psu-ip-address";
        public static final String STATE = "state";
    }

    public static class QueryKeys {
        public static final String WITH_BALANCE = "withBalance";
        public static final String LIMIT = "limit";
        public static final String OFFSET = "offset";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String STATE = "state";
    }

    public static class CountryCodes {
        public static final String NORWAY = "NO";
    }

    public static class QueryValues {
        public static final String TRUE = "true";
        public static final String BOTH = "both";
        public static final String BOOKING_STATUS = "both";
    }

    public static class Accounts {
        public static final String BALANCE_CLOSING_BOOKED = "closingBooked";
    }

    public static class HeaderKeys {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String DATE = "Date";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String SIGNATURE = "Signature";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-Uri";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String AUTHORIZATION = "AUTHORIZATION";
        public static final String TPP_ID = "TPP-ID";
        public static final String TPP_SESSION_ID = "TPP-SESSION-ID";
        public static final String PSU_ID = "PSU-ID";
        public static final String DIGEST = "Digest";
    }

    public static class HeaderValues {
        public static final String BOOKING_STATUS = "both";
        public static final String PSU_IP_ADDRESS = "134.12.51.1";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class FormValues {
        public static final String GRANT_TYPE = "client_credentials";
    }

    public static class SparebankSignSteps {
        public static final String SAMPLE_STEP = "SAMPLE_STEP";
    }

    public static class IdTags {
        public static final String RESOURCE_ID = "resourceId";
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class StorageKeys {
        public static final String ACCOUNT_ID = "account_id";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
        public static final String SESSION_ID = "SESSION_ID";
        public static final String PSU_ID = "PSU_ID";
        public static final String STATE = "STATE";
    }

    public static class DatePatterns {
        public static final String YYYY_MM_DD_PATTERN = "yyyy-MM-dd";
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String ENCODE_CERTIFICATE_ERROR = "Cannot encode certificate.";
        public static final String PAYMENT_CANT_BE_SIGNED_ERROR = "Payment cannot be signed";
        public static final String CANT_MAP_TO_PAYMENT_PRODUCT_ERROR =
                "Can not map %s to Sparebank payment status";
        public static final String MAPING_TO_TINK_PAYMENT_STATUS_ERROR =
                "Cannot map Sparebank payment status %s to Tink payment status";
        public static final String NO_ACCOUNT_TYPE_FOUND =
                "No PaymentType found for your AccountIdentifiers pair: %s";
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "sparebank";
    }
}
