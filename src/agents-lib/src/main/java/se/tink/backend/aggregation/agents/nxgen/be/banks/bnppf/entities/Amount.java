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

    public se.tink.backend.core.Amount toTinkAmount() {
        return se.tink.backend.core.Amount.inEUR(Double.valueOf(amount));
    }
}
