package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class GetTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    private String scope;

    @JsonProperty("token_type")
    private String tokenType;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, null, expiresIn);
    }
}
