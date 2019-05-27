package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class SecurityKeyRequestEntity {
    private String value;

    public SecurityKeyRequestEntity(String value) {
        this.value = value;
    }
}
