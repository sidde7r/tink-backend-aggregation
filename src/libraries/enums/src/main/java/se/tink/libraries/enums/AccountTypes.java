package se.tink.libraries.enums;

public enum AccountTypes {

    // If you update or add to these, also update the DOCUMENTED types so it's visible in the documentation.
    CHECKING, SAVINGS, INVESTMENT, MORTGAGE, CREDIT_CARD, LOAN, DUMMY, PENSION, OTHER, EXTERNAL;

    public static final String DOCUMENTED = "CHECKING, SAVINGS, INVESTMENT, MORTGAGE, CREDIT_CARD, LOAN, PENSION, OTHER, EXTERNAL";
    public static final String DOCUMENTED_TRANSFER_DESTINATION = "CHECKING, SAVINGS, INVESTMENT, CREDIT_CARD, LOAN, EXTERNAL";
    
}
