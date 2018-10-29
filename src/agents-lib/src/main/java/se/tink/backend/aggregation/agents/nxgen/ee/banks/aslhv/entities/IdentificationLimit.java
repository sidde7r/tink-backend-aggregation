package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentificationLimit {

    @JsonProperty("amount")
    private Object amount;

    @JsonProperty("enabled")
    private boolean enabled;

    @JsonProperty("free_amount")
    private Object freeAmount;

    public Object getAmount() {
        return amount;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Object getFreeAmount() {
        return freeAmount;
    }
}
