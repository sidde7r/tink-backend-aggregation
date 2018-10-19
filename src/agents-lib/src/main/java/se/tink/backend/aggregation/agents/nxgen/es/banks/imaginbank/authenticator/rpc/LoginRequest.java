package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {

    @JsonProperty("usuario")
    private String username;

    private String pin;

    private String demo;

    private String altaImagine;

    public LoginRequest(String username, String pin, String demo, String altaImagine) {
        this.username = username;
        this.pin = pin;
        this.demo = demo;
        this.altaImagine = altaImagine;
    }
}
