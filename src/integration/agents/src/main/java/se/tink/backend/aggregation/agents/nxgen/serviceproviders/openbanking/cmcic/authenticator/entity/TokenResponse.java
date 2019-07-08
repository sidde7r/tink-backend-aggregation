package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken = null;

    @JsonProperty("token_type")
    private TokenTypeEnum tokenType = null;

    @JsonProperty("expires_in")
    private Long expiresIn = null;

    @JsonProperty("refresh_token")
    private String refreshToken = null;

    @JsonProperty("scope")
    private String scope = null;

    public String getAccessToken() {
        return accessToken;
    }

    public TokenTypeEnum getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }
}
