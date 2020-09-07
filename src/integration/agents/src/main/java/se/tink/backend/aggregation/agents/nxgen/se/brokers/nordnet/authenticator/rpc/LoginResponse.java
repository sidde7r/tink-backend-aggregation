package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("session_type")
    private String sessionType;

    @JsonProperty("logged_in")
    private boolean loggedIn;

    @JsonProperty("lang")
    private String language;

    @Getter
    @JsonProperty("session_key")
    private String sessionKey;

    @JsonIgnore
    public String toBasicAuthHeader() {
        return sessionKey + ":" + sessionKey;
    }
}
