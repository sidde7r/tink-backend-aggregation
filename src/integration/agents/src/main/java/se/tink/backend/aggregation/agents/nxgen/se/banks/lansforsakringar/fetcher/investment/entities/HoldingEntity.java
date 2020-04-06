package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HoldingEntity {
    private double totalMarketValue;
    private double development;
    private double numberOfShares;
    private double purchaseValue;
    private boolean ongoingOrder;

    public double getTotalMarketValue() {
        return totalMarketValue;
    }

    public double getDevelopment() {
        return development;
    }

    public double getNumberOfShares() {
        return numberOfShares;
    }

    public double getPurchaseValue() {
        return purchaseValue;
    }

    public boolean isOngoingOrder() {
        return ongoingOrder;
    }
}
