package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionAmount {

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("currency")
    private String currency;

    public ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    public String getCurrency() {
        return currency;
    }
}
