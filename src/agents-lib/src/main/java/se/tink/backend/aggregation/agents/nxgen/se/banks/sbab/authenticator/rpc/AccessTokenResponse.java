package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class AccessTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private int expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return null;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, null, (long) expiresIn);
    }
}
