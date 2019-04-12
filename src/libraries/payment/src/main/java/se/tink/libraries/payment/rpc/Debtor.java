package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.AccountIdentifier;

public class Debtor {
    private AccountIdentifier accountIdentifier;
    private String currency;
    private String ownMessage;

    public Debtor(AccountIdentifier accountIdentifier, String currency, String ownMessage) {
        this.accountIdentifier = accountIdentifier;
        this.currency = currency;
        this.ownMessage = ownMessage;
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

    public String getOwnMessage() {
        return ownMessage;
    }
}
