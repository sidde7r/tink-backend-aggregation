package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Amount {
    private String amount;
    private String currency;

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmount() {
        return amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }
}
