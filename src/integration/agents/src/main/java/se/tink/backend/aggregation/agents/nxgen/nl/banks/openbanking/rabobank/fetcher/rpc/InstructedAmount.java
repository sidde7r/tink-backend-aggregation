package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstructedAmount {

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("sourceCurrency")
    private String sourceCurrency;

    public void setAmount(final double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setSourceCurrency(final String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }
}
