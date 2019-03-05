package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceEntity {
    private String id;

    public RegisterDeviceEntity(String deviceIdentifer) {
        this.id = deviceIdentifer;
    }
}
