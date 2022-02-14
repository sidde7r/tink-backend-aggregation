package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public final class CbiGlobeConstants {

    private CbiGlobeConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String BALANCE_NOT_FOUND = "Balance could not be found.";
        public static final String MAPPING =
                "Cannot map payment status: %s to Tink payment status.";
        public static final String RESOURCE_UNKNOWN =
                "The addressed resource is unknown relative to the TPP";
        public static final String PSU_CREDENTIALS_INVALID = "PSU_CREDENTIALS_INVALID";
    }

    public static class Urls {
        public static final String BASE_URL = "https://cbiglobeopenbankingapigateway.nexi.it";
    }

    public static class QueryKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String STATE = "state";
        public static final String BOOKING_STATUS = "booking_status";
        public static final String DATE_FROM = "date_from";
        public static final String DATE_TO = "date_to";
        public static final String TOTAL_PAGES = "cpaas-total-pages";
        public static final String OFFSET = "offset";
        public static final String SCOPE = "scope";
        public static final String RESULT = "result";
        public static final String LIMIT = "limit";
    }

    public static class QueryValues {
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String BOTH = "both";
        public static final String BOOKED = "booked";
        public static final String PRODUCTION = "production";
        public static final String SUCCESS = "success";
        public static final String FAILURE = "failure";
    }

    public static class HeaderKeys {
        public static final String ASPSP_CODE = "aspsp-code";
        public static final String ASPSP_PRODUCT_CODE = "aspsp-product-code";
        public static final String DATE = "date";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
        public static final String TPP_REDIRECT_PREFERRED = "tpp-redirect-preferred";
        public static final String TPP_NOK_REDIRECT_URI = "tpp-nok-redirect-uri";
        public static final String CONSENT_ID = "consent-id";
        public static final String PSU_IP_ADDRESS = "psu-ip-address";
        public static final String OPERATION_NAME = "operation-name";
    }

    public static class HeaderValues {
        public static final String UPDATE_PSU_DATA = "updatePsuData";
    }

    public static class FormValues {
        public static final String TRANSACTION_TYPE = "remote_transaction";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String CONSENT_ID = "consentId";
    }

    public static class PisStatus {
        public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
        public static final String AUTHENTICATED = "AUTHENTICATED";
        public static final String VERIFIED = "VERIFIED";
        public static final String FAILED = "FAILED";
    }

    public static class HttpClient {
        public static final int MAX_RETRIES = 5;
        public static final int RETRY_SLEEP_MILLISECONDS = 1000;
        public static final int RETRY_SLEEP_MILLISECONDS_SLOW_AUTHENTICATION = 3000;
    }

    public static class HttpClientParams {
        public static final int CLIENT_TIMEOUT = 60 * 1000;
    }

    public enum RequestContext {
        ACCOUNTS_GET,
        BALANCES_GET,
        CONSENT_CREATE,
        CONSENT_UPDATE,
        CONSENT_DETAILS,
        PSU_CREDENTIALS_UPDATE,
        PAYMENT_CREATE,
        PAYMENT_GET,
        PAYMENT_STATUS_GET,
        TOKEN_GET,
        TOKEN_IS_VALID,
        TRANSACTIONS_GET
    }

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
                            "TRAS")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "LLSV",
                            "ONDP",
                            "SVGS")
                    .build();
}
