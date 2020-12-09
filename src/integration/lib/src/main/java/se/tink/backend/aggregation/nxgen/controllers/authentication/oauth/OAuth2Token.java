package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OAuth2Token {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty private String scope;

    @JsonProperty private long issuedAt;

    private Map<String, String> clientSpecificProperties = new HashMap<>();

    /** Dedicated constructor for Jackson */
    private OAuth2Token() {
        this(getCurrentEpoch());
    }

    OAuth2Token(long issuedAtInSeconds) {
        this.issuedAt = issuedAtInSeconds;
    }

    public boolean hasAccessExpired() {
        long currentTime = getCurrentEpoch();
        return currentTime >= (issuedAt + expiresIn);
    }

    public boolean isValid() {
        return !hasAccessExpired();
    }

    public boolean canRefresh() {
        return !Strings.isNullOrEmpty(refreshToken);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public String getClientSpecificProperty(final String property) {
        return clientSpecificProperties.get(property);
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    private static long getCurrentEpoch() {
        return System.currentTimeMillis() / 1000L;
    }

    void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    void setScope(String scope) {
        this.scope = scope;
    }

    void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    void addClientSpecificProperties(String key, String value) {
        clientSpecificProperties.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuth2Token that = (OAuth2Token) o;
        return issuedAt == that.issuedAt
                && Objects.equals(accessToken, that.accessToken)
                && Objects.equals(tokenType, that.tokenType)
                && Objects.equals(expiresIn, that.expiresIn)
                && Objects.equals(refreshToken, that.refreshToken)
                && Objects.equals(scope, that.scope)
                && Objects.equals(clientSpecificProperties, that.clientSpecificProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                accessToken,
                tokenType,
                expiresIn,
                refreshToken,
                scope,
                issuedAt,
                clientSpecificProperties);
    }
}
