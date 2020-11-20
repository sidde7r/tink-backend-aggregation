package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public final class SparebankConstants {

    public static final String REGEX = "\\s*,\\s*";

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Current",
                            "Current",
                            "Commission",
                            "TransactingAccount",
                            "ALLTID 18-33")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Savings",
                            "SPAREKONTO")
                    .build();

    private SparebankConstants() {
        throw new AssertionError();
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "sparebank";
    }

    public static class TransactionsResponse {
        public static final int CONSECUTIVE_EMPTY_PAGES = 1;
        public static final String ERROR_MESSAGE = "scaRedirect";
        public static final int NO_OF_DAYS = 100;
    }

    public static class Urls {
        public static final String FETCH_TRANSACTIONS = "/v1/accounts/{resourceId}/transactions";
        public static final String FETCH_BALANCES = "/v1/accounts/{resourceId}/balances";
        public static final String GET_ACCOUNTS = "/v1/accounts";
        public static final String GET_SCA_REDIRECT = "/v1/bank-offered-consents";
        public static final String CREATE_PAYMENT = "/v1/payments/{paymentProduct}";
        public static final String GET_PAYMENT = "/v1/payments/{paymentProduct}/{paymentId}";
        public static final String SIGN_PAYMENT =
                "/v1/payments/{paymentProduct}/{paymentId}/authorisations";
        public static final String GET_PAYMENT_STATUS =
                "/v1/payments/{paymentProduct}/{paymentId}/status";
    }

    public static class QueryKeys {
        public static final String WITH_BALANCE = "withBalance";
        public static final String LIMIT = "limit";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String STATE = "state";
    }

    public static class QueryValues {
        public static final String TRUE = "true";
        public static final String BOTH = "both";
        public static final String BOOKING_STATUS = "both";
        public static final String TRANSACTION_LIMIT = "499";
    }

    public static class Accounts {
        public static final String BALANCE_CLOSING_BOOKED = "closingBooked";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "consent-id";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String DATE = "date";
        public static final String TPP_SIGNATURE_CERTIFICATE = "tpp-signature-certificate";
        public static final String SIGNATURE = "signature";
        public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
        public static final String PSU_IP_ADDRESS = "psu-ip-address";
        public static final String AUTHORIZATION = "authorization";
        public static final String TPP_ID = "tpp-id";
        public static final String TPP_SESSION_ID = "tpp-session-id";
        public static final String PSU_ID = "psu-id";
        public static final String DIGEST = "digest";
        public static final String ACCEPT = "accept";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class FormValues {
        public static final String GRANT_TYPE = "client_credentials";
    }

    public static class IdTags {
        public static final String RESOURCE_ID = "resourceId";
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class StorageKeys {
        public static final String ACCOUNTS = "accounts";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
        public static final String SESSION_ID = "SESSION_ID";
        public static final String PSU_ID = "PSU_ID";
        public static final String STATE = "STATE";
        public static final String TRANSACTIONS_ALL_FETCHED = "TRANSACTIONS_ALL_FETCHED";
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
        public static final String SCA_REDIRECT_MISSING = "SCA redirect missing";
        public static final String NO_AMOUNT_FOUND = "No amount found";
        public static final String INTERNATIONAL_TRANFER_NOT_SUPPORTED =
                "Cross border credit transfers are still not supported";
        public static final String DOMESTIC_FETCHING_NOT_SUPPORTED =
                "Fetching domestic payments not supported by this bank";
    }

    enum HEADERS_TO_SIGN {
        DATE("date"),
        DIGEST("digest"),
        X_REQUEST_ID("x-request-id"),
        PSU_ID("psu-id"),
        PSU_CORPORATE_ID("psu-corporate-id"),
        TPP_REDIRECT_URI("tpp-redirect-uri");

        private String header;

        HEADERS_TO_SIGN(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }
}
