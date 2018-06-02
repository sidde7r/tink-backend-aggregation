package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlacementEntity {
    private String placementType;
    private String placementTypeText;
    private List<FundHoldingsEntity> fundHoldings;

    public String getPlacementType() {
        return placementType;
    }

    public void setPlacementType(String placementType) {
        this.placementType = placementType;
    }

    public String getPlacementTypeText() {
        return placementTypeText;
    }

    public void setPlacementTypeText(String placementTypeText) {
        this.placementTypeText = placementTypeText;
    }

    public List<FundHoldingsEntity> getFundHoldings() {
        return fundHoldings;
    }

    public void setFundHoldings(
            List<FundHoldingsEntity> fundHoldings) {
        this.fundHoldings = fundHoldings;
    }
}
