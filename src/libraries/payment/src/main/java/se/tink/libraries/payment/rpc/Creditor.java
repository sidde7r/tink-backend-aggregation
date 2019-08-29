package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.AccountIdentifier;

public class Creditor {
    private AccountIdentifier accountIdentifier;
    private String name;

    public Creditor(AccountIdentifier accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public Creditor(AccountIdentifier accountIdentifier, String name) {
        this.accountIdentifier = accountIdentifier;
        this.name = name;
    }

    public AccountIdentifier.Type getAccountIdentifierType() {
        return accountIdentifier.getType();
    }

    public String getAccountNumber() {
        return accountIdentifier.getIdentifier();
    }

    public String getName() {
        return name;
    }
}
