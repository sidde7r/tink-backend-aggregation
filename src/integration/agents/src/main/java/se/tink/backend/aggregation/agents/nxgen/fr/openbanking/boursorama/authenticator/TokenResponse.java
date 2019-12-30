package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jwt.SignedJWT;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenResponse {

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("access_token")
    private SignedJWT accessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public String getTokenType() {
        return tokenType;
    }

    public SignedJWT getAccessToken() {
        return accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
