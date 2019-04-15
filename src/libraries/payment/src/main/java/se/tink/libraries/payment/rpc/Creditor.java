package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.AccountIdentifier;

public class Creditor {
    private AccountIdentifier accountIdentifier;
    private String currency;

    public Creditor(
            AccountIdentifier accountIdentifier,
            String currency) {
        this.accountIdentifier = accountIdentifier;
        this.currency = currency;
    }

    public AccountIdentifier.Type getAccountIdentifierType() {
        return accountIdentifier.getType();
    }

    public String getAccountNumber() {
        return accountIdentifier.getIdentifier();
    }

    public String getCurrency() {
        return currency;
    }
}
