package se.tink.libraries.account.enums;

public enum AccountTypes {
    CHECKING,
    SAVINGS,
    INVESTMENT,
    MORTGAGE,
    CREDIT_CARD,
    LOAN,
    DUMMY,
    PENSION,
    OTHER,
    EXTERNAL;

    public static final String DOCUMENTED_TRANSFER_DESTINATION =
            "CHECKING, SAVINGS, INVESTMENT, CREDIT_CARD, LOAN, EXTERNAL";
}
