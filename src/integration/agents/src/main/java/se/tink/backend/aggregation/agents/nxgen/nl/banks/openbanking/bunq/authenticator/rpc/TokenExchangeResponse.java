package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import static se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants.Token.ACCESS_TOKEN_EXPIRES_IN_SECONDS;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenExchangeResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    private String state;

    public String getTokenType() {
        return tokenType;
    }

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, null, ACCESS_TOKEN_EXPIRES_IN_SECONDS);
    }
}
