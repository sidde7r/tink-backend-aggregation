package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Card {

    private String cardHolderName;
    private String maskedNr;
    private String id;
    private String issueDate;
    private String expiryDate;
    private String status;
    private boolean isPrimary;
    private boolean canChangePin;
    private boolean isCardCreatedRecently;
    private boolean isCardReplacementAllowed;

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getMaskedNr() {
        return maskedNr;
    }

    public boolean isPrimary() {
        return isPrimary;
    }
}
