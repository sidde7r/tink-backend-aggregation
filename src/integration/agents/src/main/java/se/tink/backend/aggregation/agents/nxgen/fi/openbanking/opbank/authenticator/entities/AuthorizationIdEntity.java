package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationIdEntity {
    private String value;
    private boolean essential;

    public AuthorizationIdEntity(String value, boolean essential) {
        this.value = value;
        this.essential = essential;
    }
}
