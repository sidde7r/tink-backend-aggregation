package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardsResponse {
    @JsonProperty("Output")
    private CardListResponse output;

    public CardListResponse getOutput() {
        return output;
    }

    public void setOutput(CardListResponse output) {
        this.output = output;
    }
}
