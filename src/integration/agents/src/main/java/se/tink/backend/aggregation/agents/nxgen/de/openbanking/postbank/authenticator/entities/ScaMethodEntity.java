package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMethodEntity {

    private String name;

    private String authenticationType;

    private String authenticationMethodId;

    public String getName() {
        return name;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }

    public String toString() {
        return "Name: " + name + ", Type: " + authenticationType;
    }
}
