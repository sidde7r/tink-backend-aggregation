package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
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

    @JsonProperty("scope")
    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    private String getRefreshToken() {
        return refreshToken;
    }

    private Long getExpiresIn() {
        return Long.parseLong(expiresIn);
    }

    public String getScope() {
        return scope;
    }

    public OAuth2Token toOauthToken() {
        return OAuth2Token.create(
                IcaBankenConstants.QueryKeys.BEARER,
                getAccessToken(),
                getRefreshToken(),
                getExpiresIn());
    }
}
