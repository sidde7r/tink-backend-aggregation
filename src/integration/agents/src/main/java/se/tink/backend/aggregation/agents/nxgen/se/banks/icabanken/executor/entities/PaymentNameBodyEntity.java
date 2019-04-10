package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentNameBodyEntity {
    @JsonProperty("Found")
    private boolean found;

    @JsonProperty("OnAlertList")
    private boolean onAlertList;

    @JsonProperty("Name")
    private String name;

    public boolean isFound() {
        return found;
    }

    public boolean isOnAlertList() {
        return onAlertList;
    }

    public String getName() {
        return name;
    }
}
