package se.tink.backend.aggregation.agents.banks.nordea.v20.model.beneficiaries;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BeneficiaryListResponse {

    @JsonProperty("getBeneficiaryListOut")
    private BeneficiaryListOut beneficiaryListOut;

    public BeneficiaryListOut getBeneficiaryListOut() {
        return beneficiaryListOut;
    }

    public void setBeneficiaryListOut(BeneficiaryListOut beneficiaryListOut) {
        this.beneficiaryListOut = beneficiaryListOut;
    }
}
