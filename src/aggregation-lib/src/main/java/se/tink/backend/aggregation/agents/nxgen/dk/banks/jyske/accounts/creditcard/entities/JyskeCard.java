package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JyskeCard {

    private String id;
    private String maskedCardNumber;
    private JyskeCardType cardType;
    private JyskeCardAccount account;

    public boolean isCreditCard() {
        return cardType != null && cardType.isCreditCard();
    }
}
