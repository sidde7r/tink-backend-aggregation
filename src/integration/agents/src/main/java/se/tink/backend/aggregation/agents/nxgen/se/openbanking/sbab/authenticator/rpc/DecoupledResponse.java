package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class DecoupledResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("auth_method")
    private String authMethod;

    @JsonProperty("user_token")
    private String userToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public OAuth2Token getAccessToken() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, expiresIn);
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }
}
