package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MortgageSignatureStatusResponse {

    @JsonProperty("status")
    private String status;

    public MortgageSignatureStatus getStatus() {
        return MortgageSignatureStatus.fromStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
