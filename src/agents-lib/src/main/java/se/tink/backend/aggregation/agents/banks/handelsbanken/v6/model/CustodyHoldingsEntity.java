package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustodyHoldingsEntity extends AbstractResponse {
    private SecurityIdentifierEntity securityIdentifier;
    private String name;
    private AmountEntity marketValue;
    private QuantityEntity holdingQuantity;
    private PerformanceEntity performance;
    private AmountEntity marketPrice;
    private AmountEntity averagePurchasePrice;

    public SecurityIdentifierEntity getSecurityIdentifier() {
        return securityIdentifier;
    }

    public void setSecurityIdentifier(
            SecurityIdentifierEntity securityIdentifier) {
        this.securityIdentifier = securityIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(AmountEntity marketValue) {
        this.marketValue = marketValue;
    }

    public QuantityEntity getHoldingQuantity() {
        if (holdingQuantity == null) {
            holdingQuantity = new QuantityEntity();
        }
        return holdingQuantity;
    }

    public void setHoldingQuantity(
            QuantityEntity holdingQuantity) {
        this.holdingQuantity = holdingQuantity;
    }

    public PerformanceEntity getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceEntity performance) {
        this.performance = performance;
    }

    public AmountEntity getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(AmountEntity marketPrice) {
        this.marketPrice = marketPrice;
    }

    public AmountEntity getAveragePurchasePrice() {
        return averagePurchasePrice;
    }

    public void setAveragePurchasePrice(
            AmountEntity averagePurchasePrice) {
        this.averagePurchasePrice = averagePurchasePrice;
    }
}
