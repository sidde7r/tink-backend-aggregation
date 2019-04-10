package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    private String state;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return Long.parseLong(expiresIn);
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

    @JsonIgnore
    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, getExpiresIn());
    }
}
