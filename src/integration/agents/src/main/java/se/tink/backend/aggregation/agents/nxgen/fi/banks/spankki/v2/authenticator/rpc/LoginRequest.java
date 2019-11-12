package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.entities.CredentialsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {
    @JsonProperty private CredentialsEntity credentials = new CredentialsEntity();

    @JsonIgnore
    public void setUsername(String username) {
        credentials.setUsername(username);
    }

    @JsonIgnore
    public void setPassword(String password) {
        credentials.setPassword(password);
    }

    @JsonIgnore
    public void setPin(String pin) {
        credentials.setPin(pin);
    }

    @JsonIgnore
    public void setToken(String token) {
        credentials.setToken(token);
    }
}
