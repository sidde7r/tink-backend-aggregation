package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMethodEntity {

    private String authenticationType;
    private String authenticationMethodId;
    private DataEntity name;
    private DataEntity explanation;

    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }

    public String toString() {
        return "Name: " + name + ", Type: " + authenticationType;
    }
}
