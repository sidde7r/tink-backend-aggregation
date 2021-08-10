package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class PaymentAccessToken {
    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("refresh_token")
    String refreshToken;

    @JsonProperty("token_type")
    String tokenType;

    @JsonProperty("expires_in")
    long expiresIn;

    String scope;

    public OAuth2Token toOauth2Token() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, expiresIn);
    }
}
