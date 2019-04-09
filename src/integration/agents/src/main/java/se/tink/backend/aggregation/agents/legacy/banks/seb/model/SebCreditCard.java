package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// PCBW3242
@JsonIgnoreProperties(ignoreUnknown = true)
public class SebCreditCard {
    @JsonProperty("ROW_ID")
    public String ROW_ID;

    @JsonProperty("CARD_NO")
    public String CARD_NO;

    @JsonProperty("NAME_ON_CARD")
    public String NAME_ON_CARD;
}
