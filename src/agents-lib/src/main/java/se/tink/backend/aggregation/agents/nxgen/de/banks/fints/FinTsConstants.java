package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class FinTsConstants {

    public static final String CURRENCY = "EUR";
    public static final String TINK_FINTS_REGISTERATION = "9DA0690B610D90C2CD37E262F";

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
        public static final String ACCOUNT_NOT_ASSIGNED = "9010";
        public static final String INVALID_USERNAME_FORMAT = "9130";
        public static final String ING_DIBA_ACCOUNT_BLOCKED = "9931";
    }

    public static class StatusMessage {
        public static final String END_DATE_NOT_SUPPORTED = "Angabe eines Endedatums nicht unterstützt.";
        public static final String NO_ACTIVE_PHONE_NUMBER_WARNING = "Keine aktive Mobilfunknr.Bitte wenden Sie sich an Ihren Berater";
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

    public static class AccountType {
        public final static int CHECKING_ACCOUNT_CURSOR = 1; //  Kontokorrent-/Girokonto 1 - 9
        public final static int SAVINGS_ACCOUNT_CURSOR = 10; //  Sparkonto 10 - 19
        public final static int TIME_DEPOSIT_ACCOUNT_CURSOR = 20; // Festgeldkonto(Termineinlagen) 20 - 29
        public final static int SECURITES_ACCOUNT_CURSOR = 30; // Werpapierdpot 30 - 39
        public final static int LOAN_ACCOUNT_CURSOR = 40; //Kredit-/Darlehenskonto 40 - 49
        public final static int CREDIT_CARD_CURSOR = 50; // Kreditkartenkonto 50 - 59
        public final static int FUND_DEPOSIT_ACCOUNT_CURSOR = 60; // Fonds-Depot bei einer Kapitalanlagegesellschaft 60 - 69
        public final static int BAUSPAR_ACCOUNT_CURSOR = 70; //  Bausparvertrag 70 - 79
        public final static int INSURANCE_CONTRACT_CURSOR = 80; // Versicherungsvertrag 80 - 89
        public final static int OTHER_NOT_ASSIGNABLE_CURSOR = 90; // Sonstige (nicht zuordenbar) 90 - 99
    }

    public static class SegData {
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

    public static class SepaAccountIdentifiers {
        public static final ImmutableList<String> KNOWN_SAVINGS_ACCOUNT_NAMES = ImmutableList.of("extra-konto", "sparbrief", "vl-sparen", "tagesgeld plus");
        public static final ImmutableList<String> ACCOUNT_TYPE_SAVINGS_TOKENS = ImmutableList.of("spar");
        public static final ImmutableList<String> KNOWN_INVESTMENT_ACCOUNT_NAMES = ImmutableList.of("direkt-depot", "cfd konto", "o/f-konto");
        public static final ImmutableList<String> ACCOUNT_TYPE_INVESTMENT_TOKENS = ImmutableList.of("depot", "cfd", "o/f");
        public static final ImmutableList<String> KNOWN_CHECKING_ACCOUNT_NAMES = ImmutableList.of("girokonto", "kontokorrentkonto privat", "lohn/gehalt/rente privat", "verrechnungskonto");
        public static final ImmutableList<String> KNOWN_CREDIT_ACCOUNT_NAMES = ImmutableList.of("visa prepaid-karte", "visa-karte");
        public static final ImmutableList<String> ACCOUNT_TYPE_CREDIT_TOKENS = ImmutableList.of("credit", "visa");
    }

    public static class LogTags {
        public static final LogTag ERROR_CODE = LogTag.from("#fints_login_error_types");
        public static final LogTag PRODUCTNAME_FOR_MISSING_ACCOUNT_TYPE = LogTag.from("#fints_missing_account_type");
    }

    public static class Storage {
        public static final String REG_NUMBER = "regNumber";
    }
}
