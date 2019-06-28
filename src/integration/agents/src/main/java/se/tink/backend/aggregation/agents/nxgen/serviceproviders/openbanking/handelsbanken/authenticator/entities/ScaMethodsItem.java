package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMethodsItem {

    @JsonProperty("_links")
    private Links links;

    @JsonProperty("scaMethodType")
    private String scaMethodType;

    public Links getLinks() {
        return links;
    }

    public String getScaMethodType() {
        return scaMethodType;
    }
}
