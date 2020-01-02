package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardDetailsEntity {
    private String cardHolderName;
    private String pcidssToken;
    private String maskedCardNo;
    private Number isPrimaryCard;
    private String issueDate;
    private String expiryDate;
    private String cardStatus;

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getMaskedCardNo() {
        return maskedCardNo;
    }
}
