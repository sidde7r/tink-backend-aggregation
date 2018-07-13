package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingEntity {
    private double totalMarketValue;
    private double development;
    private double numberOfShares;
    private double purchaseValue;
    private boolean ongoingOrder;

    public double getTotalMarketValue() {
        return totalMarketValue;
    }

    public void setTotalMarketValue(double totalMarketValue) {
        this.totalMarketValue = totalMarketValue;
    }

    public double getDevelopment() {
        return development;
    }

    public void setDevelopment(double development) {
        this.development = development;
    }

    public double getNumberOfShares() {
        return numberOfShares;
    }

    public void setNumberOfShares(double numberOfShares) {
        this.numberOfShares = numberOfShares;
    }

    public double getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(double purchaseValue) {
        this.purchaseValue = purchaseValue;
    }

    public boolean isOngoingOrder() {
        return ongoingOrder;
    }

    public void setOngoingOrder(boolean ongoingOrder) {
        this.ongoingOrder = ongoingOrder;
    }
}
