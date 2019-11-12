package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CredentialsEntity {
    @JsonProperty private String username = "";
    @JsonProperty private String password = "";
    @JsonProperty private String pin = "";
    @JsonProperty private String token = "";

    @JsonIgnore
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public void setPin(String pin) {
        this.pin = pin;
    }

    @JsonIgnore
    public void setToken(String token) {
        this.token = token;
    }
}
