package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

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

    public AccountIdentifierType getAccountIdentifierType() {
        return accountIdentifier.getType();
    }

    public String getAccountNumber() {
        return accountIdentifier.getIdentifier();
    }

    public String getName() {
        return name;
    }

    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }
}
