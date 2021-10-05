package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.AccountIdentifier;

public class Creditor extends PaymentParty {
    private String name;

    public Creditor(AccountIdentifier accountIdentifier) {
        this(accountIdentifier, null);
    }

    public Creditor(AccountIdentifier accountIdentifier, String name) {
        super(accountIdentifier);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
