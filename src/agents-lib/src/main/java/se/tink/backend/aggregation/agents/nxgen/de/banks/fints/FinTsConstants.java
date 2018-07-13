package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class FinTsConstants {

    public static final String CURRENCY = "EUR";

    public static class StatusCode {
        public static final String SUCCESS = "0020";
        public static final String NO_ENTRY = "3010";
        public static final String TAN_VERSION = "3920";
        public static final String PIN_TEMP_BLOCKED = "3931";
        public static final String MORE_INFORMATION_AVAILABLE = "3040";
        public static final String NO_DATA_AVAILABLE = "9910";
        public static final String TECHNICAL_ERROR = "9110";
        public static final String INVALID_USER = "9000";
        public static final String INVALID_PIN = "9942";
        public static final String LOGIN_FAILED = "9210";
        public static final String ACTION_LOCKED = "9010";

    }

    public static class Segments {
        public static final String HKSAL = "HKSAL"; // Balance Request
        public static final String HISAL = "HISAL"; // Balance Response
        public static final String HKSPA = "HKSPA"; // SEPA information Request
        public static final String HISPA = "HISPA"; // SEPA information Response
        public static final String HKIDN = "HKIDN"; // Identification
        public static final String HKSYN = "HKSYN"; // Synchronization Request
        public static final String HISYN = "HISYN"; // Synchronization Response
        public static final String HKVVB = "HKVVB"; // Processing preparation
        public static final String HKEND = "HKEND"; // End session
        public static final String HNHBK = "HNHBK"; // Request Header
        public static final String HNHBS = "HNHBS"; // News completion
        public static final String HNSHA = "HNSHA"; // Signature hash
        public static final String HNSHK = "HNSHK"; // Signature Header
        public static final String HNVSD = "HNVSD"; // Encrypted Data
        public static final String HNVSK = "HNVSK"; // Encrypted Header
        public static final String HIRMS = "HIRMS"; // Feedback on Segments
        public static final String HIRMG = "HIRMG"; // Global feedback
        public static final String HKKAZ = "HKKAZ"; // Statements/Transactions Request
        public static final String HIKAZ = "HIKAZ"; // Statements/Transactions Response
        public static final String HISALS = "HISALS"; // Balance query parameter
        public static final String HIKAZS = "HIKAZS"; // Account sales/period parameter
        public static final String HIUPD = "HIUPD"; // Account information
    }

    public static class SegData {
        public final static String PRODUCT_NAME = "tink";
        public final static String PRODUCT_VERSION = "0.1";
        public final static String CUSTOMER_ID = "1";
        public final static String COUNTRY_CODE = "280"; // Germany
        public final static int LANGUAGE_STANDARD = 0;
        public final static int LANGUAGE_DE = 1;
        public final static int LANGUAGE_EN = 2;
        public final static int LANGUAGE_FR = 3;

        public final static int DEFAULT_UPD_VERSION = 0; // User-Parameter-Data
        public final static int DEFAULT_BPD_VERSION = 0; // Bank-Parameter-Data

        public final static int SECURITY_BOUNDARY = 1; // SHM
        public final static int SECURITY_SUPPLIER_ROLE = 1; // ISS
        public final static int COMPRESSION_NONE = 0;
        public final static String SEGMENT_DELIMITED = "'";
        public final static String ELEMENT_DELIMITER = ":";
        public final static String GROUP_DELIMITER = "+";

        public final static String MT940_TURNOVER_FIELD = ":61:";
        public final static String MT940_MULTIPURPOSE_FIELD = ":86:";
    }

    public static class LogTags {
        public static final LogTag ERROR_CODE = LogTag.from("#fints_login_error_types");
    }
}
