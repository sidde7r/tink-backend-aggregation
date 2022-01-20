package se.tink.agent.sdk.models.payments.payment;

import se.tink.libraries.account.AccountIdentifier;

public class Debtor {
    private final AccountIdentifier accountIdentifier;

    public Debtor(AccountIdentifier accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }
}
