package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FinTsConstants {

    public static final String CURRENCY = "EUR";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StatusCode {
        public static final String SUCCESS = "0020";
        public static final String TAN_GENERATED_SUCCESSFULLY = "0030";
        public static final String NO_ENTRY = "3010";
        public static final String PIN_TEMP_BLOCKED = "3931";
        public static final String MORE_INFORMATION_AVAILABLE = "3040";
        public static final String STRONG_CUSTOMER_AUTHORIZATION_REQUIRED = "3075";
        public static final String WEAK_CUSTOMER_AUTHORIZATION_ALLOWED = "3076";
        public static final String APPROVED_TAN_PROCEDURES = "3920";
        public static final String NO_DATA_AVAILABLE = "9910";
        public static final String TECHNICAL_ERROR = "9110";
        public static final String INVALID_USER = "9000";
        public static final String INVALID_PIN = "9942";
        public static final String LOGIN_FAILED = "9210";
        public static final String ACTION_LOCKED = "9010";
        public static final String ACCOUNT_NOT_ASSIGNED = "9010";
        public static final String INVALID_USERNAME_FORMAT = "9130";
        public static final String ING_DIBA_ACCOUNT_BLOCKED = "9931";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StatusMessage {
        public static final String END_DATE_NOT_SUPPORTED =
                "Angabe eines Endedatums nicht unterstützt.";
        public static final String NO_ACTIVE_PHONE_NUMBER_WARNING =
                "Keine aktive Mobilfunknr.Bitte wenden Sie sich an Ihren Berater";
    }
}
