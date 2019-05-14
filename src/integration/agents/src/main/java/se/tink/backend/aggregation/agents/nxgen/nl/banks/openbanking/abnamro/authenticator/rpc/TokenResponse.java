package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;

    private String state;

    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getState() {
        return state;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public OAuth2Token toOauthToken() {
        return OAuth2Token.create(
                getTokenType(), getAccessToken(), getRefreshToken(), getExpiresIn());
    }
}
