package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {
    private String amount;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(String amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }
}
