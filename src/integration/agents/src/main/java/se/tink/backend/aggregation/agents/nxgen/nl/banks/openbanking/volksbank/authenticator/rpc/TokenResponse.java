package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {
    @Getter
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private String expiresIn;

    @Getter
    @JsonProperty("token_type")
    private String tokenType;

    @Getter
    @JsonProperty("scope")
    private String scope;

    @Getter
    @JsonProperty("refresh_token")
    private String refreshToken;

    public long getExpiresIn() {
        return Long.parseLong(expiresIn);
    }

    @JsonIgnore
    public OAuth2Token toOauthToken() {
        return OAuth2Token.create(
                getTokenType(), getAccessToken(), getRefreshToken(), getExpiresIn());
    }
}
