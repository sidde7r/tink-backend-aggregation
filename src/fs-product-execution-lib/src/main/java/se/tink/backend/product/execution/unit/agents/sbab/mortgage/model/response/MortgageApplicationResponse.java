package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MortgageApplicationResponse {

    @JsonProperty("sbabId")
    private String sbabId;

    public String getSbabId() {
        return sbabId;
    }

    public void setSbabId(String sbabId) {
        this.sbabId = sbabId;
    }
}
