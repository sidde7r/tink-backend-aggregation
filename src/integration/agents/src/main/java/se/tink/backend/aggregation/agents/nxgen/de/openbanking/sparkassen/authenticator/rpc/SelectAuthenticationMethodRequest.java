package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SelectAuthenticationMethodRequest {

    private String authenticationMethodId;

    public SelectAuthenticationMethodRequest(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
