package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {

    private String tokenType;

    private String accessToken;

    private long expiresIn;

    private String consentedOn;

    @JsonProperty private String scope;

    private String refreshToken;

    private long refreshTokenExpiresIn;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, expiresIn);
    }
}
