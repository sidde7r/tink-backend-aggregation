package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityCode {

    private String code;

    private SecurityCode() {
    }

    public static SecurityCode create(String code) {
        return new SecurityCode().setCode(code);
    }

    private SecurityCode setCode(String code) {
        this.code = code;
        return this;
    }
}
