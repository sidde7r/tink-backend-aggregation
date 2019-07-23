package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class FinTsConstants {
    public static final String INTEGRATION_NAME = "fints";
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
        public static final String END_DATE_NOT_SUPPORTED =
                "Angabe eines Endedatums nicht unterstützt.";
        public static final String NO_ACTIVE_PHONE_NUMBER_WARNING =
                "Keine aktive Mobilfunknr.Bitte wenden Sie sich an Ihren Berater";
    }

    public enum Segments {
        HKSAL, // Balance Request
        HISAL, // Balance Response
        HKSPA, // SEPA information Request
        HISPA, // SEPA information Response
        HKIDN, // Identification
        HKSYN, // Synchronization Request
        HISYN, // Synchronization Response
        HKVVB, // Processing preparation
        HKEND, // End session
        HNHBK, // Request Header
        HNHBS, // News completion
        HNSHA, // Signature hash
        HNSHK, // Signature Header
        HNVSD, // Encrypted Data
        HNVSK, // Encrypted Header
        HIRMS, // Feedback on Segments
        HIRMG, // Global feedback
        HKKAZ, // Statements/Transactions Request
        HIKAZ, // Statements/Transactions Response
        HKWPD, // Depot Request
        HIWPD, // Depot Response
        HISALS, // Balance query parameter
        HIKAZS, // Account sales/period parameter
        HIUPD; // Account information
    }

    public static class AccountType {
        public static final int CHECKING_ACCOUNT_CURSOR = 1; //  Kontokorrent-/Girokonto 1 - 9
        public static final int SAVINGS_ACCOUNT_CURSOR = 10; //  Sparkonto 10 - 19
        public static final int TIME_DEPOSIT_ACCOUNT_CURSOR =
                20; // Festgeldkonto(Termineinlagen) 20 - 29
        public static final int SECURITES_ACCOUNT_CURSOR = 30; // Werpapierdpot 30 - 39
        public static final int LOAN_ACCOUNT_CURSOR = 40; // Kredit-/Darlehenskonto 40 - 49
        public static final int CREDIT_CARD_CURSOR = 50; // Kreditkartenkonto 50 - 59
        public static final int FUND_DEPOSIT_ACCOUNT_CURSOR =
                60; // Fonds-Depot bei einer Kapitalanlagegesellschaft 60 - 69
        public static final int BAUSPAR_ACCOUNT_CURSOR = 70; //  Bausparvertrag 70 - 79
        public static final int INSURANCE_CONTRACT_CURSOR = 80; // Versicherungsvertrag 80 - 89
        public static final int OTHER_NOT_ASSIGNABLE_CURSOR =
                90; // Sonstige (nicht zuordenbar) 90 - 99
    }

    public static class SegData {
        public static final String PRODUCT_VERSION = "0.1";
        public static final String CUSTOMER_ID = "1";
        public static final String COUNTRY_CODE = "280"; // Germany
        public static final int LANGUAGE_STANDARD = 0;
        public static final int LANGUAGE_DE = 1;
        public static final int LANGUAGE_EN = 2;
        public static final int LANGUAGE_FR = 3;

        public static final int DEFAULT_UPD_VERSION = 0; // User-Parameter-Data
        public static final int DEFAULT_BPD_VERSION = 0; // Bank-Parameter-Data

        public static final int SECURITY_BOUNDARY = 1; // SHM
        public static final int SECURITY_SUPPLIER_ROLE = 1; // ISS
        public static final int COMPRESSION_NONE = 0;
        public static final String SEGMENT_DELIMITED = "'";
        public static final String ELEMENT_DELIMITER = ":";
        public static final String GROUP_DELIMITER = "+";

        public static final String MT940_TURNOVER_FIELD = ":61:";
        public static final String MT940_MULTIPURPOSE_FIELD = ":86:";
    }

    public static class SepaAccountIdentifiers {
        public static final ImmutableList<String> KNOWN_SAVINGS_ACCOUNT_NAMES =
                ImmutableList.of("extra-konto", "sparbrief", "vl-sparen", "tagesgeld plus");
        public static final ImmutableList<String> ACCOUNT_TYPE_SAVINGS_TOKENS =
                ImmutableList.of("spar");
        public static final ImmutableList<String> KNOWN_INVESTMENT_ACCOUNT_NAMES =
                ImmutableList.of("direkt-depot", "cfd konto", "o/f-konto");
        public static final ImmutableList<String> ACCOUNT_TYPE_INVESTMENT_TOKENS =
                ImmutableList.of("depot", "cfd", "o/f");
        public static final ImmutableList<String> KNOWN_CHECKING_ACCOUNT_NAMES =
                ImmutableList.of(
                        "girokonto",
                        "kontokorrentkonto privat",
                        "lohn/gehalt/rente privat",
                        "verrechnungskonto");
        public static final ImmutableList<String> KNOWN_CREDIT_ACCOUNT_NAMES =
                ImmutableList.of("visa prepaid-karte", "visa-karte");
        public static final ImmutableList<String> ACCOUNT_TYPE_CREDIT_TOKENS =
                ImmutableList.of("credit", "visa");
    }

    public static class LogTags {
        public static final LogTag ERROR_CODE = LogTag.from("#fints_login_error_types");
        public static final LogTag PRODUCTNAME_FOR_MISSING_ACCOUNT_TYPE =
                LogTag.from("#fints_missing_account_type");
        public static final LogTag ERROR_CANNOT_FETCH_ACCOUNT_BALANCE =
                LogTag.from("#fints_cannot_fetch_balance");
        public static final LogTag ERROR_CANNOT_FETCH_ACCOUNT_TRANSACTIONS =
                LogTag.from("#fints_cannot_fetch_transactions");
    }

    public static class Storage {
        public static final String REG_NUMBER = "regNumber";
    }
}
