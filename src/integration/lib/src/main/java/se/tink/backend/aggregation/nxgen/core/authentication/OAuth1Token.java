package se.tink.backend.aggregation.nxgen.core.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OAuth1Token {

    private String oauthToken;
    private String oauthTokenSecret;
    private String oauthCallbackConfirmed;
    private String oauthVerifier;

    public OAuth1Token(
            @JsonProperty("oauthToken") String oauthToken,
            @JsonProperty("oauthTokenSecret") String oauthTokenSecret,
            @JsonProperty("oauthCallbackConfirmed") String oauthCallbackConfirmed,
            @JsonProperty("oauthVerifier") String oauthVerifier) {
        this.oauthToken = oauthToken;
        this.oauthTokenSecret = oauthTokenSecret;
        this.oauthCallbackConfirmed = oauthCallbackConfirmed;
        this.oauthVerifier = oauthVerifier;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public String getOauthTokenSecret() {
        return oauthTokenSecret;
    }

    public String getOauthCallbackConfirmed() {
        return oauthCallbackConfirmed;
    }

    public String getOauthVerifier() {
        return oauthVerifier;
    }
}
