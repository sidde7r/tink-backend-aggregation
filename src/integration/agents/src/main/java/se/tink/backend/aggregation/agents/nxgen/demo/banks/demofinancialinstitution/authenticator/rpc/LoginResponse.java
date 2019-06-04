package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {

    private String token;
    private String status;

    public String getToken() {
        return token;
    }

    public String getStatus() {
        return status;
    }
}
