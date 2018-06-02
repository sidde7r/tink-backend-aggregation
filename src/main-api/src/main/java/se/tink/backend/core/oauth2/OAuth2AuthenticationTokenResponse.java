package se.tink.backend.core.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuth2AuthenticationTokenResponse {
    @JsonProperty("token_type")
    private String tokenType = "bearer";
    @JsonProperty("expires_in")
    private int expiresIn;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("scope")
    private String scope;

    public OAuth2AuthenticationTokenResponse() {

    }

    public OAuth2AuthenticationTokenResponse(OAuth2Authorization authorization) {
        this.accessToken = authorization.getAccessToken();
        this.refreshToken = authorization.getRefreshToken();
        this.scope = authorization.getScope();
        this.expiresIn = (int) (OAuth2Authorization.ACCESS_TOKEN_TIMEOUT / 1000);
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
