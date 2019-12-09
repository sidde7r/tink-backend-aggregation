package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private String expiresIn;

    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getExpiresIn() {
        return Long.parseLong(expiresIn);
    }

    public String getScope() {
        return scope;
    }

    public OAuth2Token toOauthToken() {
        return OAuth2Token.create(
                SocieteGeneraleConstants.HeaderValues.BEARER,
                getAccessToken(),
                getRefreshToken(),
                getExpiresIn());
    }
}
