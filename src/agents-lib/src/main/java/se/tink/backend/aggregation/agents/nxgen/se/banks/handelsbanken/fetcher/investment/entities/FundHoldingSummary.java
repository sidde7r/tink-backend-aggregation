package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundHoldingSummary {
    private double marketValue;
    //Not sure that purchaseValue is always returned... It's not part of Handelsbanken App, but seems to be part of
    // the server response at the moment (2018-01-19)
    private double purchaseValue;

    public double getMarketValue() {
        return marketValue;
    }

    public double getPurchaseValue() {
        return purchaseValue;
    }
}
