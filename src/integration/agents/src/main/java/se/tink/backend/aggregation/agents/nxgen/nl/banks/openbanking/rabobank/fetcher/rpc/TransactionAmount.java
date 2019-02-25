package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionAmount {

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("currency")
    private String currency;

    public void setAmount(final double amount) {
        this.amount = amount;
    }

    public Amount getAmount() {
        return new Amount(currency, amount);
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }
}
