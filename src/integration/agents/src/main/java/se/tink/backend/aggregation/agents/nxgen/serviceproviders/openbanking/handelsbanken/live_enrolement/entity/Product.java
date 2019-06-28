package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Product {

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("name")
    private String name;

    @JsonProperty("version")
    private String version;

    public String getScope() {
        return scope;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
