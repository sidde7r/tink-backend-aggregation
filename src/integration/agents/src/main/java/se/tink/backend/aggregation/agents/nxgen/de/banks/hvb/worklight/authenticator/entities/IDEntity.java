package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class IDEntity {
    private String entity;
    private boolean allowed;
    private String token;

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public boolean getAllowed() {
        return allowed;
    }
}
