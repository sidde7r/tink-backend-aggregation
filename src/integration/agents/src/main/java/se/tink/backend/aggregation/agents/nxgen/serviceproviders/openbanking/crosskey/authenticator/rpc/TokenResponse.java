package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("id_token")
    private String idToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private long expiresIn;

    public OAuth2Token toTinkToken() {
        return OAuth2Token
            .create(tokenType, accessToken, refreshToken, Instant.now().getEpochSecond(),
                expiresIn);
    }
}
