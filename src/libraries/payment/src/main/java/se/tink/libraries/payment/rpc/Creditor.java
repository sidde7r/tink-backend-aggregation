package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.AccountIdentifier;

public class Creditor {
    private AccountIdentifier accountIdentifier;
    private String currency;
    private String messageToCreditor;
    private String creditorName;

    public Creditor(
            AccountIdentifier accountIdentifier,
            String currency,
            String messageToCreditor,
            String creditorName) {
        this.accountIdentifier = accountIdentifier;
        this.currency = currency;
        this.messageToCreditor = messageToCreditor;
        this.creditorName = creditorName;
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

    public String getMessageToCreditor() {
        return messageToCreditor;
    }

    public String getCreditorName() {
        return creditorName;
    }
}
