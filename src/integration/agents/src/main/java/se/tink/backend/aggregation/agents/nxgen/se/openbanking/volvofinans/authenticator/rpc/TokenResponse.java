package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(StorageKeys.BEARER, accessToken, null, expiresIn);
    }
}
