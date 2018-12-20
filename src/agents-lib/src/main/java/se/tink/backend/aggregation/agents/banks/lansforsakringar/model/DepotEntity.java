package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DepotEntity {
    private String depotNumber;
    private String totalGrowthInRealValue;
    private String totalValue;
    private String totalGrowthInPercent;

    public String getDepotNumber() {
        return depotNumber;
    }

    public void setDepotNumber(String depotNumber) {
        this.depotNumber = depotNumber;
    }

    public Double getTotalGrowthInRealValue() {
        return totalGrowthInRealValue == null || totalGrowthInRealValue.isEmpty() ?
                null : StringUtils.parseAmount(totalGrowthInRealValue);
    }

    public void setTotalGrowthInRealValue(String totalGrowthInRealValue) {
        this.totalGrowthInRealValue = totalGrowthInRealValue;
    }

    public Double getTotalValue() {
        return totalValue == null || totalValue.isEmpty() ? null : StringUtils.parseAmount(totalValue);
    }

    public void setTotalValue(String totalValue) {
        this.totalValue = totalValue;
    }

    public String getTotalGrowthInPercent() {
        return totalGrowthInPercent;
    }

    public void setTotalGrowthInPercent(String totalGrowthInPercent) {
        this.totalGrowthInPercent = totalGrowthInPercent;
    }

    public Portfolio toPortfolio(Double marketValue, Double cashValue) {
        return toPortfolio(marketValue, cashValue, Portfolio.Type.DEPOT);
    }

    public Portfolio toPortfolio(Double marketValue, Double cashValue, Portfolio.Type type) {
        Portfolio portfolio = new Portfolio();

        portfolio.setCashValue(cashValue);
        portfolio.setUniqueIdentifier(getDepotNumber());
        portfolio.setTotalValue(marketValue);
        portfolio.setTotalProfit(getTotalGrowthInRealValue());
        portfolio.setType(type);

        return portfolio;
    }
}
