package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SelectAuthenticationMethodRequest {

    private String authenticationMethodId;

    public SelectAuthenticationMethodRequest(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
