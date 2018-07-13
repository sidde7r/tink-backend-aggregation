package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class CardDetailsResponse {
    @JsonProperty("getCardDetailsOut")
    private CardDetailsEntity cardDetails;

    public CardDetailsEntity getCardDetails() {
        return cardDetails;
    }

    public void setCardDetails(CardDetailsEntity cardDetails) {
        this.cardDetails = cardDetails;
    }
}
