package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class TokenResponse {
    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, expiresIn);
    }
}
