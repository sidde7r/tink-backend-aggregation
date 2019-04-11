package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc;

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
    private String
            expiresIn; // TODO: Verify that this is sent in seconds. Should be 90 days based on
    // documentation

    private String scope;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, Long.parseLong(expiresIn));
    }
}
