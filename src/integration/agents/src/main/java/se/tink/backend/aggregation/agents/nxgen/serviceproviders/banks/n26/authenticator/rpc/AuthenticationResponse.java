package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResponse {

    private String mfaToken;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    private String scope;
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    public String getMfaToken() {
        return mfaToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @JsonIgnore
    // TODO: Time Unit needs to be confirmed!!
    public LocalDateTime getExpiresAt() {
        return LocalDateTime.now().plus(expiresIn, ChronoUnit.DAYS);
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public TokenEntity getToken() {
        return new TokenEntity(accessToken, refreshToken, getExpiresAt());
    }
}
