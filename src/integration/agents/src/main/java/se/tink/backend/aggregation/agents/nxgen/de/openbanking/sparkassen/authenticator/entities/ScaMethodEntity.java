package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMethodEntity {

    private String authenticationType;
    private String authenticationVersion;
    private String authenticationMethodId;
    private String name;
    private String explanation;

    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public String getName() {
        return name;
    }
}
