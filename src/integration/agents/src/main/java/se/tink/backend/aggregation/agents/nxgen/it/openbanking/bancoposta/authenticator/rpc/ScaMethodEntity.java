package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc;

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
        return authenticationType;
    }
}
