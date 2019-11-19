package se.tink.backend.aggregation.agents.brokers.nordnet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Base64;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AnonymousLoginPasswordResponse {

    @JsonProperty("country")
    private String country;

    @JsonProperty("environment")
    private String environment;

    @JsonProperty("session_key")
    private String sessionKey;

    @JsonProperty("private_feed")
    private PrivateFeed privateFeed;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("public_feed")
    private PublicFeed publicFeed;

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

    public void setPrivateFeed(PrivateFeed privateFeed) {
        this.privateFeed = privateFeed;
    }

    public PrivateFeed getPrivateFeed() {
        return privateFeed;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setPublicFeed(PublicFeed publicFeed) {
        this.publicFeed = publicFeed;
    }

    public PublicFeed getPublicFeed() {
        return publicFeed;
    }

    @JsonIgnore
    public String toBasicAuthHeader() {
        return "Basic "
                + Base64.getEncoder().encodeToString((sessionKey + ":" + sessionKey).getBytes());
    }
}
