package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {
    private double amount;
    private String currency;

    public double getAmount(boolean isNegative) {
        if(isNegative) {
            return amount * -1;
        }
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
