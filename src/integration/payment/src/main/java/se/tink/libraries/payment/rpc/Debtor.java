package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class Debtor {
    private AccountIdentifier accountIdentifier;

    public Debtor(AccountIdentifier accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public AccountIdentifierType getAccountIdentifierType() {
        return accountIdentifier.getType();
    }

    public String getAccountNumber() {
        return accountIdentifier.getIdentifier();
    }

    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }
}
