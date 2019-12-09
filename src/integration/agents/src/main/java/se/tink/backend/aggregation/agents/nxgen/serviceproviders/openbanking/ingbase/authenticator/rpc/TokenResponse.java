package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_token_expires_in")
    private long refreshTokenExpiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("client_id")
    private String clientId;

    // issuedAt is needed because the application access token will be saved as json string to the
    // provider
    private long issuedAt = getCurrentEpoch();

    public OAuth2Token toTinkToken() {
        long currentTime = getCurrentEpoch();
        this.expiresIn = this.issuedAt + this.expiresIn - currentTime;
        this.refreshTokenExpiresIn = this.issuedAt + this.refreshTokenExpiresIn - currentTime;
        return OAuth2Token.create(
                this.tokenType,
                this.accessToken,
                this.refreshToken,
                this.expiresIn,
                this.refreshTokenExpiresIn);
    }

    public OAuth2Token toTinkToken(OAuth2Token token) {
        token.updateAccessToken(this.accessToken, this.expiresIn);
        return token;
    }

    public String getScope() {
        return scope;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean hasAccessExpired() {
        long currentTime = getCurrentEpoch();
        return currentTime >= this.issuedAt + this.expiresIn;
    }

    private static long getCurrentEpoch() {
        return System.currentTimeMillis() / 1000L;
    }
}
