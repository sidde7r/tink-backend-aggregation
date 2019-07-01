package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SchemaEntity {

    @JsonProperty("type")
    private String type;

    @JsonProperty("properties")
    private PropertiesEntity propertiesEntity;

    public SchemaEntity(String type, PropertiesEntity properties) {
        this.type = type;
        this.propertiesEntity = properties;
    }

    public String getType() {
        return type;
    }

    public PropertiesEntity getPropertiesEntity() {
        return propertiesEntity;
    }
}
