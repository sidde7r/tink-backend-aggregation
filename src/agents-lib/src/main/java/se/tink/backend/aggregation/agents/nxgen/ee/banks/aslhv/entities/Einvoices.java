package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Einvoices {

    @JsonProperty("total")
    private int total;

    @JsonProperty("need_action")
    private int needAction;

    public int getTotal() {
        return total;
    }

    public int getNeedAction() {
        return needAction;
    }
}
