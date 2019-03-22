package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class DeviceEntity {
    private String environment;
    private String id;
    private String model;
    private String os;

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setOs(String os) {
        this.os = os;
    }
}
