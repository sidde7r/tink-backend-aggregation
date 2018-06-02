package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentSavingsDepotEntity {
    private String totalValue;
    private String totalAcquisitionCost;
    private String totalGrowthInPercent;
    private String totalGrowthInRealValue;
    private List<InvestmentSavingsDepotWrappersEntity> investmentSavingsDepotWrappers;

    public String getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(String totalValue) {
        this.totalValue = totalValue;
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

    public List<InvestmentSavingsDepotWrappersEntity> getInvestmentSavingsDepotWrappers() {
        return investmentSavingsDepotWrappers;
    }

    public void setInvestmentSavingsDepotWrappers(
            List<InvestmentSavingsDepotWrappersEntity> investmentSavingsDepotWrappers) {
        this.investmentSavingsDepotWrappers = investmentSavingsDepotWrappers;
    }
}
