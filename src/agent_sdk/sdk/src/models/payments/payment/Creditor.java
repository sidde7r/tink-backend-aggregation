package se.tink.agent.sdk.models.payments.payment;

import se.tink.libraries.account.AccountIdentifier;

public class Creditor {
    private final String name;
    private final AccountIdentifier accountIdentifier;

    public Creditor(AccountIdentifier accountIdentifier, String name) {
        this.accountIdentifier = accountIdentifier;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }
}
