package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class GetTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("consented_on")
    private Long consentedOn;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_token_expires_in")
    private Long refreshTokenExpiresIn;

    private String scope;

    @JsonProperty("token_type")
    private String tokenType;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(
                tokenType, accessToken, refreshToken, expiresIn, refreshTokenExpiresIn);
    }
}
