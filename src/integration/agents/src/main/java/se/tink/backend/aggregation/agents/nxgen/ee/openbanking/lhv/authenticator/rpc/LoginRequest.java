package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {

    private String authenticationMethodId;

    public LoginRequest(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
