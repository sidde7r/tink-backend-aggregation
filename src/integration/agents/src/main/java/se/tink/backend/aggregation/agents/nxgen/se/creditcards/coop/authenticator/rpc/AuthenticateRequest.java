package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticateRequest {
    private String username;
    private String password;

    public AuthenticateRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
