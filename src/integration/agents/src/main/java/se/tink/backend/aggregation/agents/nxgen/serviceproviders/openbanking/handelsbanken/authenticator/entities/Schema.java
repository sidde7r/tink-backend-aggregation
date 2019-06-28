package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Schema {

    @JsonProperty("type")
    private String type;

    @JsonProperty("properties")
    private Properties properties;

    public Schema(String type, Properties properties) {
        this.type = type;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public Properties getProperties() {
        return properties;
    }
}
