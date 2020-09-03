package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class InitLoginResponse {

    @JsonProperty("expires_in")
    private String expiresIn;

    private String environment;
    private String country;

    @JsonProperty("session_type")
    private String sessionType;

    @JsonProperty("logged_in")
    private boolean loggedIn;

    @JsonProperty("lang")
    private String language;

    @Getter
    @JsonProperty("session_id")
    private String sessionId;

    @JsonIgnore
    public String toBasicAuthHeader() {
        return sessionId + ":" + sessionId;
    }
}
