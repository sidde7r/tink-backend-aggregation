package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("username")
    private String user;

    @JsonProperty("displayName")
    private String username;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty(".issued")
    private String issued;

    @JsonProperty(".expires")
    private String expires;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, expiresIn);
    }
}
