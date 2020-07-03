package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationStatusResponse {
    private String scaStatus;
    private String authorizationCode;

    public String getScaStatus() {
        return scaStatus;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }
}
