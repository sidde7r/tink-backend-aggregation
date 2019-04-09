package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAmount {

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    public double getAmount() {
        return Double.parseDouble(amount);
    }

    public void setAmount(final String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "BalanceAmount{"
                + "amount = '"
                + amount
                + '\''
                + ",currency = '"
                + currency
                + '\''
                + "}";
    }
}
