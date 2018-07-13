package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServiceEntity {
    private String name;
    private String availability;

    public String getName() {
        return name;
    }

    public String getAvailability() {
        return availability;
    }
}
