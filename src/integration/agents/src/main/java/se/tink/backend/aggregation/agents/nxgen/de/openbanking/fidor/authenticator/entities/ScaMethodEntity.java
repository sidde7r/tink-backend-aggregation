package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMethodEntity {
    private String authenticationMethodId;

    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }
}
