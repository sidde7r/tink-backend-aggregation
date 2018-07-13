package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankdataDepositAssetEntity {
    private double numberOfShares;
    private double stockValue;
    private double numberOfTradable;
    private String customerBaseCurrency;

    public double getNumberOfShares() {
        return numberOfShares;
    }

    public double getStockValue() {
        return stockValue;
    }

    public double getNumberOfTradable() {
        return numberOfTradable;
    }

    public String getCustomerBaseCurrency() {
        return customerBaseCurrency;
    }

    public double getQuantity() {
        return numberOfTradable + numberOfShares;
    }
}
