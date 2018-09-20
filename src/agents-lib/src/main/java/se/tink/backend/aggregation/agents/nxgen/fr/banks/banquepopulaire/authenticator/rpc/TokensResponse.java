package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokensResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private int expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }
}
