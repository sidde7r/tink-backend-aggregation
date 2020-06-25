package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessTokenResponseEntity {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    public String getAccessToken() {
        return accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }
}
