package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class GetTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("not-before-policy")
    private Long notBeforePolicy;

    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String scope;

    @JsonProperty("session_state")
    private String sessionState;

    @JsonProperty("token_type")
    private String tokenType;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(
                tokenType, accessToken, refreshToken, expiresIn, refreshExpiresIn);
    }
}
