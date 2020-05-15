package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenEntity {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("refresh_expires_in")
    private Integer refreshExpiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("jti")
    private String jti;

    public String getToken() {
        return accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public String getJti() {
        return jti;
    }

    public OAuth2Token toOAuth2Token() {
        return OAuth2Token.create(
                tokenType, accessToken, refreshToken, expiresIn, refreshExpiresIn);
    }
}
