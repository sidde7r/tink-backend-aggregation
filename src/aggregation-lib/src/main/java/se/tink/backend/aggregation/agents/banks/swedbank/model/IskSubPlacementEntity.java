package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IskSubPlacementEntity {
    private String name;
    private List<IskHoldingEntity> holdings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IskHoldingEntity> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<IskHoldingEntity> holdings) {
        this.holdings = holdings;
    }
}
