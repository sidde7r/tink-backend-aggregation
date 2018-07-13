package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {
    private String formattedAmount;
    private double rawValue;

    public String getFormattedAmount() {
        return formattedAmount;
    }

    public double getRawValue() {
        return rawValue;
    }
}
