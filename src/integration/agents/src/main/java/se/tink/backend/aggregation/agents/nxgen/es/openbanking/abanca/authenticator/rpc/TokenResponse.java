package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("user")
    private String user;

    @JsonProperty("username")
    private String username;

    @JsonProperty("authorized_apis")
    private String authorizedApis;

    @JsonProperty(".issued")
    private String issued;

    @JsonProperty(".expires")
    private String expires;

    public String getAccessToken() {
        return accessToken;
    }
}
