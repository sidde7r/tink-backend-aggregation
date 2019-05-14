package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {
    private String currency;
    private Double amount;

    public BalanceEntity() {}

    public String getCurrency() {
        return currency;
    }

    public Double getAmount() {
        return amount;
    }
}
