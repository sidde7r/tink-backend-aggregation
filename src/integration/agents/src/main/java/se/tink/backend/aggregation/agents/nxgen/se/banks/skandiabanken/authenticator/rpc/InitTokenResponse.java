package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitTokenResponse {

    @JsonProperty("access_token")
    private String accessToken = "";

    @JsonProperty("token_type")
    private String tokenType = "";

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonIgnore
    public String getAccessToken() {
        return accessToken;
    }
}
