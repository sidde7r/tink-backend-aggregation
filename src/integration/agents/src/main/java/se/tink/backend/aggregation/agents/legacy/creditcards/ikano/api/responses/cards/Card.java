package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements.CardType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {
    @JsonProperty("_Expires")
    private String expiresAt;

    @JsonProperty("_CreditAgreement")
    private String hasCredit;

    @JsonProperty("_ObjectGroupCode")
    private String groupCode;

    @JsonProperty("_ProductCode")
    private String cardType;

    @JsonProperty("_State")
    private String state;

    public boolean isOfType(CardType cardType) {
        return cardType.hasIdentifier(this.cardType);
    }

    public boolean isRegistered() {
        return Objects.equals(state.toLowerCase(), "registered");
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String hasCredit() {
        return hasCredit;
    }

    public void setHasCredit(String hasCredit) {
        this.hasCredit = hasCredit;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
