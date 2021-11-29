package se.tink.agent.sdk.models.account;

public enum AccountCreditType {
    // `Credit Limit` is the total amount of credit given to this account.
    LIMIT,
    // `Available credit` is the *unused portion* of a borrower's credit limit.
    AVAILABLE,
}
