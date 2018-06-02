package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.bankIdReference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    private String reference;

    public String getReference() {
        return reference;
    }

    @JsonProperty("_Reference")
    public void setReference(String reference) {
        this.reference = reference;
    }
}
