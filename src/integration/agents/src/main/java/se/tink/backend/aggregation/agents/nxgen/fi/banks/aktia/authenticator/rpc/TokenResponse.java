package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    private String scope;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonIgnore
    public OAuth2Token getToken() {
        return OAuth2Token.create(tokenType, accessToken, null, expiresIn);
    }
}
