package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import java.time.LocalDate;
import java.util.regex.Pattern;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public final class FiduciaConstants {

    public static class Patterns {

        public static final Pattern STARTCODE_CHIP_PATTERN =
                Pattern.compile("Startcode\\s\"(\\d+)");
        public static final Pattern CHIP_TAN_INSTRUCTION_LINE_DELIMITER =
                Pattern.compile("(<br>)+");
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "consent-id";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "consent-id";
        public static final String TPP_SIGNATURE_CERTIFICATE = "tpp-signature-certificate";
        public static final String SIGNATURE = "signature";
        public static final String DIGEST = "digest";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String PSU_ID = "psu-id";
        public static final String ACCEPT = "accept";
        public static final String TPP_REDIRECT_URI = "tpp-redirect";
        public static final String PSU_IP_ADDRESS = "psu-ip-address";
    }

    public static class QueryParamsKeys {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
    }

    public static class QueryParamsValues {
        public static final String BOOKING_STATUS = "booked";
        public static final String DATE_FROM = "1970-01-01";
    }

    public static class FormValues {
        public static final LocalDate VALID_UNTIL = LocalDate.of(9999, 12, 31);
        public static final int FREQUENCY_PER_DAY = 4;
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String OTHER_ID = "123";
        public static final String SCHEME_NAME = "PISP";
    }

    public static class CredentialKeys {
        public static final String PSU_ID = "psu-id";
    }

    public static class ErrorMessageKeys {
        public static final String PSU_CREDENTIALS_INVALID = "PSU_CREDENTIALS_INVALID";
        public static final String NO_ACCOUNT_AVAILABLE =
                "There is no activation for XS2A or there are no accounts available for access. Please contact your bank (SERVICE_BLOCKED)";
        public static final String TAN_PLUS_BLOCKED =
                "Sm@rt-TAN plus blocked. Please contact your bank (SERVICE_BLOCKED)";
        public static final String ONLINE_ACCESS_BLOCKED =
                "Online access blocked. Please contact your bank (SERVICE_BLOCKED)";
        public static final String PIN_CHANGE_REQUIRED = "PIN change required (SERVICE_BLOCKED)";
        public static final String ERROR_KONF = "ERR_KONF_CSV_BANK_MISS";
        public static final String SECURE_GO_BLOCKED =
                "SecureGo usage blocked. Please contact your bank (SERVICE_BLOCKED)";
        public static final String TAN_NOT_VALID =
                "No valid TAN procedure registered. Please contact your bank (SERVICE_BLOCKED)";
        public static final String PHONE_TAN_BLOCKED =
                "Phone locked for mobileTAN. Please contact your bank (SERVICE_BLOCKED)";
        public static final String ORDER_NOT_PROCESSED_OR_REJECTED =
                "Order could not be processed and was rejected. (SERVICE_BLOCKED)";

        public static final String ORDER_LIMIT_EXCEEDED =
                "Order not executed because limit exceeded";
        public static final String MISSING_COVERAGE = "Missing coverage (PAYMENT_FAILED)";
        public static final String ORDER_BLOCKED =
                "Payment order rejected due to block (PAYMENT_FAILED)";
        public static final String ORDER_REJECTED = "Payment order rejected (PAYMENT_FAILED)";
        public static final String READ_TIME_OUT = "Read timed out";
        public static final String ORDER_DUPLICATED =
                "Order rejected because this order has already been submitted (SERVICE_BLOCKED)";
        public static final String NO_PAYMENT_AUTHORIZATION =
                "No authorization for transaction (SERVICE_BLOCKED)";
    }

    public static class EndUserErrorMessageKeys {
        public static final LocalizableKey UNAVAILABLE_ACCOUNT_MESSAGE =
                new LocalizableKey(
                        "There are no accounts available for access. Please contact your bank.");
        public static final LocalizableKey BLOCKED_ACCESS_MESSAGE =
                new LocalizableKey("Online access blocked. Please contact your bank.");
        public static final LocalizableKey BLOCKED_TAN_MESSAGE =
                new LocalizableKey("Sm@rt-TAN plus blocked. Please contact your bank.");
        public static final LocalizableKey BANK_NO_LONGER_AVAILABLE_MESSAGE =
                new LocalizableKey("Bank is no longer available.");
        public static final LocalizableKey ORDER_NOT_PROCESSED_MESSAGE =
                new LocalizableKey(
                        "There may be a lock on the TAN procedure. Please contact your bank.");
        public static final LocalizableKey PHONE_TAN_BLOCKED_MESSAGE =
                new LocalizableKey("MobileTAN is blocked. Please contact your bank.");
        public static final LocalizableKey TAN_NOT_VALID_MESSAGE =
                new LocalizableKey("No valid TAN procedure registered. Please contact your bank.");
        public static final LocalizableKey SECURE_GO_BLOCKED_MESSAGE =
                new LocalizableKey("SecureGo usage blocked. Please contact your bank.");
    }
}
