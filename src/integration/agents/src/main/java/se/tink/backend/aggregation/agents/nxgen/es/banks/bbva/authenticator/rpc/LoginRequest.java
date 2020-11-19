package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities.AuthenticationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {
    private AuthenticationEntity authentication;

    public LoginRequest(String username, String password, String multistepProcessId) {
        this.authentication = new AuthenticationEntity(username, password, multistepProcessId);
    }

    public LoginRequest(String username, String multistepProcessId) {
        this.authentication = new AuthenticationEntity(username, multistepProcessId);
    }
}
