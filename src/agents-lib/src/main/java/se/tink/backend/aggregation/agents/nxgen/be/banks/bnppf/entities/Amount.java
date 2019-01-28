package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

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

    public se.tink.libraries.amount.Amount toTinkAmount() {
        return se.tink.libraries.amount.Amount.inEUR(Double.valueOf(amount));
    }
}
