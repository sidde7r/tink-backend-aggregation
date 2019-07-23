package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class OAuth2TokenResponse {

    @JsonProperty("access_token")
    private String accessToken = "";

    @JsonProperty("token_type")
    private String tokenType = "";

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken = "";

    @JsonProperty("offline_token")
    private String offlineToken = "";

    @JsonProperty("offline_refresh_token")
    private String offlineRefreshToken = "";

    @JsonIgnore
    public OAuth2Token toOAuth2Token() {
        return OAuth2Token.create(
                this.tokenType, this.accessToken, this.refreshToken, this.expiresIn);
    }
}
