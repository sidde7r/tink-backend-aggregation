package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {
    private double amount;
    private String currency;
    private boolean isMainCurrency;
    private boolean isOriginalCurrency;

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean getMainCurrency() {
        return isMainCurrency;
    }

    public boolean getOriginalCurrency() {
        return isOriginalCurrency;
    }
}
