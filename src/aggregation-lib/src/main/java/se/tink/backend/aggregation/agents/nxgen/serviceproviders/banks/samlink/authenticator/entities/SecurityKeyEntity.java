package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityKeyEntity {
    private String value;

    public void setValue(String value) {
        this.value = value;
    }
}
