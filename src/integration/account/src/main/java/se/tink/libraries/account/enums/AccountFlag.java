package se.tink.libraries.account.enums;

public enum AccountFlag {
    BUSINESS,
    MANDATE,
    PSD2_PAYMENT_ACCOUNT,
    // DEPOT_CASH_BALANCE - A flag added for displaying deposit accounts to investment accounts.
    // Those deposit accounts are usually parsed as savings accounts.
    // This is introduced due to a request from Avanza.
    DEPOT_CASH_BALANCE
}
