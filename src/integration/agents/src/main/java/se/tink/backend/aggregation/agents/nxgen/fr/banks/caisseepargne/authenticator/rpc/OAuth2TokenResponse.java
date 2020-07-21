package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class OAuth2TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonIgnore
    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, null, expiresIn);
    }
}
