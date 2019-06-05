package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {

    public String username;
    public String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
