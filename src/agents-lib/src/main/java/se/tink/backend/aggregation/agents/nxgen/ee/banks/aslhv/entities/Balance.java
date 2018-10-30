package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Balance {

    @JsonProperty("currency_id")
    private int currencyId;

    @JsonProperty("free_amount")
    private double freeAmount;

    public int getCurrencyId() {
        return currencyId;
    }

    public double getFreeAmount() {
        return freeAmount;
    }
}
