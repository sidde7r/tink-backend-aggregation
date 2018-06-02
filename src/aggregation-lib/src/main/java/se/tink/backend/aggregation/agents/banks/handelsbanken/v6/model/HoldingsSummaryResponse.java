package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingsSummaryResponse extends AbstractResponse {
    private AmountEntity marketValue;
    private PerformanceEntity performance;
    private List<CustodyAccountEntity> custodyAccounts;

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(AmountEntity marketValue) {
        this.marketValue = marketValue;
    }

    public PerformanceEntity getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceEntity performance) {
        this.performance = performance;
    }

    public List<CustodyAccountEntity> getCustodyAccounts() {
        return custodyAccounts;
    }

    public void setCustodyAccounts(
            List<CustodyAccountEntity> custodyAccounts) {
        this.custodyAccounts = custodyAccounts;
    }
}
