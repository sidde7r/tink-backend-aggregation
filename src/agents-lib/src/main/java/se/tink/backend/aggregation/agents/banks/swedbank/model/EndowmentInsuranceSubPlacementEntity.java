package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndowmentInsuranceSubPlacementEntity {
    private String name;
    private List<EndowmentInsuranceHoldingEntity> holdings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EndowmentInsuranceHoldingEntity> getHoldings() {
        return holdings;
    }

    public void setHoldings(
            List<EndowmentInsuranceHoldingEntity> holdings) {
        this.holdings = holdings;
    }
}
