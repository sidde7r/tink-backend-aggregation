package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String type;

    @JsonProperty("validation_token")
    private String validationToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(type, accessToken, "", expiresIn);
    }
}
