package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountEntity {
    private String id;
    private double creditLimit;
    private double purchaseLimit;
    private double currentBalance;
    private double nonBilledAmount;
    private double disposableAmount;
    private String state;
    private String currencyCode;

    public String getId() {
        return id;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public double getPurchaseLimit() {
        return purchaseLimit;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public double getNonBilledAmount() {
        return nonBilledAmount;
    }

    public double getDisposableAmount() {
        return disposableAmount;
    }

    public String getState() {
        return state;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
