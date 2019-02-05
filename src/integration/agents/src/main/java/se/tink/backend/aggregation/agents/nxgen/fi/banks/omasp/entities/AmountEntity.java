package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private String display;
    private Long value;

    public String getDisplay() {
        return display;
    }

    public Double getValue() {
        return value / 100.0;
    }

    public Amount toTinkAmount() {
        return Amount.inEUR(getValue());
    }
}
