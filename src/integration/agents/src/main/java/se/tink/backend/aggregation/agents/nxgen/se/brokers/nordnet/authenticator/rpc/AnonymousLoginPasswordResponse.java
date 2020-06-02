package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AnonymousLoginPasswordResponse {

    @JsonProperty("country")
    private String country;

    @JsonProperty("environment")
    private String environment;

    @JsonProperty("session_key")
    private String sessionKey;

    @JsonProperty("expires_in")
    private int expiresIn;

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    @JsonIgnore
    public String toBasicAuthHeader() {
        return sessionKey + ":" + sessionKey;
    }
}
