package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {

    private String authenticationMethodId;
    private String scaAuthenticationData;

    public LoginRequest(String authenticationMethodId, String scaAuthenticationData) {
        this.authenticationMethodId = authenticationMethodId;
        this.scaAuthenticationData = scaAuthenticationData;
    }
}
