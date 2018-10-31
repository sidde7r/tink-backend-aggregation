package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Balance {

    @JsonProperty("currency_id")
    private int currencyId;

    @JsonProperty("free_credit_amount")
    private double freeCreditAmount;

    @JsonProperty("free_amount")
    private double freeAmount;

    @JsonIgnore
    public int getCurrencyId() {
        return currencyId;
    }

    @JsonIgnore
    public double getFreeAmount() {
        return freeAmount;
    }

    @JsonIgnore
    public double getFreeCreditAmount() {
        return freeCreditAmount;
    }
}
