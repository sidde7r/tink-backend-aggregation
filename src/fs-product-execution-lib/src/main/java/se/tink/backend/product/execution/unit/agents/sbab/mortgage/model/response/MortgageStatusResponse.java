package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MortgageStatusResponse {

    @JsonProperty("status")
    private String status;

    public MortgageStatus getStatus() {
        return MortgageStatus.valueOf(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
