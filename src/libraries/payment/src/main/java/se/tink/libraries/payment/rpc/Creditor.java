package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.AccountIdentifier;

public class Creditor {
    private AccountIdentifier accountIdentifier;

    public Creditor(AccountIdentifier accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public AccountIdentifier.Type getAccountIdentifierType() {
        return accountIdentifier.getType();
    }

    public String getAccountNumber() {
        return accountIdentifier.getIdentifier();
    }
}
