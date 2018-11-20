package se.tink.backend.aggregation.agents.banks.seb.utilities;

public class SEBBankIdLoginUtils {
    // Strings for status comparison when logging in with BankID.
    public static final String START_BANKID = "rfa1";
    public static final String ALREADY_IN_PROGRESS = "rfa3";
    public static final String USER_CANCELLED = "rfa6";
    public static final String NO_CLIENT = "rfa8";
    public static final String USER_SIGN = "rfa9";
    public static final String AUTHENTICATED = "rfa100";
    public static final String AUTHORIZATION_REQUIRED = "rfa101";
    public static final String COLLECT_BANKID = "rfa102";

    // Status strings
    public static final String ALREADY_IN_PROGRESS_STATUS = "e0";
}
