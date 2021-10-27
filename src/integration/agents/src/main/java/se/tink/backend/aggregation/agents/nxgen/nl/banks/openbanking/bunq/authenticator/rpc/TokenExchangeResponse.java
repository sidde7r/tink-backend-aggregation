package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants.Token;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenExchangeResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @Getter
    @JsonProperty("token_type")
    private String tokenType;

    private String state;

    @JsonIgnore
    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(
                tokenType, accessToken, null, Token.ACCESS_TOKEN_EXPIRES_IN_SECONDS);
    }
}
