package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class OpBankAmountEntity {
    private double amount;
    private String currencyCode;

    @JsonIgnore
    public ExactCurrencyAmount getTinkAmount() {
        return ExactCurrencyAmount.of(amount, currencyCode);
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
