package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Securities {

    @JsonProperty("total")
    private int total;

    public int getTotal() {
        return total;
    }
}
