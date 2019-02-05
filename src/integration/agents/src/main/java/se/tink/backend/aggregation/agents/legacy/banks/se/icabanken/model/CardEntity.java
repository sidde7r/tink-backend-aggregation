package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardEntity {
    @JsonProperty("CardId")
    private String cardId;
    @JsonProperty("CardType")
    private String cardType;
    @JsonProperty("Expires")
    private String expires;
    @JsonProperty("IsEditable")
    private boolean isEditable;
    @JsonProperty("IsUnrestrictedCard")
    private boolean isUnrestrictedCard;
    @JsonProperty("MaskedCardNumber")
    private String maskedCardNumber;
    @JsonProperty("NameOnCard")
    private String nameOnCard;
    @JsonProperty("NeedsActivation")
    private boolean needsActivation;

    public CardEntity() {

    }

    public String getCardId() {
        return cardId;
    }

    public String getCardType() {
        return cardType;
    }

    public String getExpires() {
        return expires;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public boolean isNeedsActivation() {
        return needsActivation;
    }

    public boolean isUnrestrictedCard() {
        return isUnrestrictedCard;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    public void setNeedsActivation(boolean needsActivation) {
        this.needsActivation = needsActivation;
    }

    public void setUnrestrictedCard(boolean isUnrestrictedCard) {
        this.isUnrestrictedCard = isUnrestrictedCard;
    }
}
