package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityHoldingsEntity {
    private String totalMarketValue;
    private String totalAcquisitionCost;
    private String totalGrowthInPercent;
    private String totalGrowthInRealValue;
    private List<IskFundEntity> funds;
    private List<ShareEntity> shares;
    private List<BondEntity> bonds;

    public String getTotalMarketValue() {
        return totalMarketValue;
    }

    public void setTotalMarketValue(String totalMarketValue) {
        this.totalMarketValue = totalMarketValue;
    }

    public String getTotalAcquisitionCost() {
        return totalAcquisitionCost;
    }

    public void setTotalAcquisitionCost(String totalAcquisitionCost) {
        this.totalAcquisitionCost = totalAcquisitionCost;
    }

    public String getTotalGrowthInPercent() {
        return totalGrowthInPercent;
    }

    public void setTotalGrowthInPercent(String totalGrowthInPercent) {
        this.totalGrowthInPercent = totalGrowthInPercent;
    }

    public String getTotalGrowthInRealValue() {
        return totalGrowthInRealValue;
    }

    public void setTotalGrowthInRealValue(String totalGrowthInRealValue) {
        this.totalGrowthInRealValue = totalGrowthInRealValue;
    }

    public List<IskFundEntity> getFunds() {
        return funds != null ? funds : Collections.emptyList();
    }

    public void setFunds(List<IskFundEntity> funds) {
        this.funds = funds;
    }

    public List<ShareEntity> getShares() {
        return shares != null ? shares : Collections.emptyList();
    }

    public void setShares(List<ShareEntity> shares) {
        this.shares = shares;
    }

    public List<BondEntity> getBonds() {
        return bonds != null ? bonds : Collections.emptyList();
    }

    public void setBonds(List<BondEntity> bonds) {
        this.bonds = bonds;
    }
}
