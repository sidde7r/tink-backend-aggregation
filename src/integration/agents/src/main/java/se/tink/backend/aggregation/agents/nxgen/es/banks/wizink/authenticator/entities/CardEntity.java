package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {
    private String cardNumber;
    private String accountNumber;

    public String getCardNumber() {
        return cardNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
