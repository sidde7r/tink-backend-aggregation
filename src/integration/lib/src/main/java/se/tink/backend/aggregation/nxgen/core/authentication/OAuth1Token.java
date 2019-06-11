package se.tink.backend.aggregation.nxgen.core.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OAuth1Token {

    @JsonProperty("oauthToken")
    private String oauthToken;

    @JsonProperty("oauthTokenSecret")
    private String oauthTokenSecret;

    @JsonProperty("oauthCallbackConfirmed")
    private String oauthCallbackConfirmed;

    @JsonProperty("oauthVerifier")
    private String oauthVerifier;

    public OAuth1Token() {}

    public OAuth1Token(
            String oauthToken,
            String oauthTokenSecret,
            String oauthCallbackConfirmed,
            String oauthVerifier) {
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
