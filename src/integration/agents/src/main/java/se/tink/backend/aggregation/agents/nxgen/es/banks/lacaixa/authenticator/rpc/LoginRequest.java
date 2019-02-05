package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {

    @JsonProperty("usuario")
    private String username;

    private String pin;

    public LoginRequest(String username, String pin) {
        this.username = username;
        this.pin = pin;
    }
}
