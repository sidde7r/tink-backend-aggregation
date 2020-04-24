package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class Amount {
    private String amount;
    private String currency;

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.inEUR(Double.parseDouble(amount));
    }
}
