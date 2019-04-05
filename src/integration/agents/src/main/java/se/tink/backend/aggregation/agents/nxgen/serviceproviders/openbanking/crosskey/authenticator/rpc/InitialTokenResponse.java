package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitialTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    public String getAccessToken() {
        return accessToken;
    }
}
