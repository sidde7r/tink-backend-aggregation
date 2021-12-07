package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.exception.RequiredDataMissingException;
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
        return Optional.ofNullable(accessToken)
                .orElseThrow(() -> new RequiredDataMissingException("Missing access token"));
    }

    private String getRefreshToken() {
        return Optional.ofNullable(refreshToken)
                .orElseThrow(() -> new RequiredDataMissingException("Missing refresh token"));
    }

    private Long getExpiresIn() {
        return Optional.ofNullable(expiresIn)
                .map(Long::parseLong)
                .orElseThrow(
                        () -> new RequiredDataMissingException("Missing token expiration time"));
    }

    public String getScope() {
        return scope;
    }

    public OAuth2Token toOauthToken() {
        return OAuth2Token.create(
                NorwegianConstants.QueryKeys.BEARER,
                getAccessToken(),
                getRefreshToken(),
                getExpiresIn());
    }
}
