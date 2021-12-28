package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String expiresIn;

    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return Long.parseLong(expiresIn);
    }

    public String getScope() {
        return scope;
    }

    public OAuth2Token toOauthToken() {
        return OAuth2Token.create(
                getTokenType(), getAccessToken(), getRefreshToken(), getExpiresIn());
    }
}
