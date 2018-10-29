package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Balance {

    @JsonProperty("reserved_amount")
    private double reservedAmount;

    @JsonProperty("free_credit_amount")
    private double freeCreditAmount;

    @JsonProperty("currency_id")
    private int currencyId;

    @JsonProperty("free_amount")
    private double freeAmount;

    public double getReservedAmount() {
        return reservedAmount;
    }

    public double getFreeCreditAmount() {
        return freeCreditAmount;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public double getFreeAmount() {
        return freeAmount;
    }
}
