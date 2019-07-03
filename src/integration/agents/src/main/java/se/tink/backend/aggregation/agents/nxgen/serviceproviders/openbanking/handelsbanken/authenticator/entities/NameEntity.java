package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NameEntity {

    private String description;

    private String type;

    private String example;

    public NameEntity(String description, String type, String example) {
        this.description = description;
        this.type = type;
        this.example = example;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getExample() {
        return example;
    }
}
