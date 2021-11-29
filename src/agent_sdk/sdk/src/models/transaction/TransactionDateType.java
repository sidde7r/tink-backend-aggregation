package se.tink.agent.sdk.models.transaction;

public enum TransactionDateType {
    // The date when an entry is posted to an account on the ASPSPs books.
    BOOKING,
    // The Date at which assets become available to the account owner in case of a credit.
    VALUE,
    // Date and time at which the transaction was executed.
    EXECUTION,
    // Date on which the amount of money ceases to be available to the agent that owes it and when
    // the amount of money
    // becomes available to the agent to which it is due.
    INTERBANK_SETTLEMENT
}
