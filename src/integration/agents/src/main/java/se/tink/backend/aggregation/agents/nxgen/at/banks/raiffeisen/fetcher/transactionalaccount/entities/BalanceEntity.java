package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {
    private Double amount;
    private String currency;

    public Double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
