package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {

    @JsonProperty("AccessToken")
    private String accessToken;

    @JsonProperty("RefreshToken")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
