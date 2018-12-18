package se.tink.backend.aggregation.nxgen.core.authentication;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OAuth2Token {
    @JsonIgnore
    private static final String BEARER = "bearer";

    private String tokenType;
    private String accessToken;
    @JsonProperty
    private String refreshToken;
    private long expiresInSeconds;
    private long refreshExpiresInSeconds;
    private long issuedAt;

    private OAuth2Token(@JsonProperty("tokenType") String tokenType,
            @JsonProperty("accessToken") String accessToken,
            @JsonProperty("refreshToken") String refreshToken,
            @JsonProperty("expiresInSeconds") long expiresInSeconds,
            @JsonProperty("refreshExpiresInSeconds") long refreshExpiresInSeconds,
            @JsonProperty("issuedAt") long issuedAt) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresInSeconds = expiresInSeconds;
        this.refreshExpiresInSeconds = refreshExpiresInSeconds;
        this.issuedAt = issuedAt;
    }

    @JsonIgnore
    public static OAuth2Token create(String tokenType, String accessToken, String refreshToken,
            long accessExpiresInSeconds) {
        return new OAuth2Token(
                tokenType,
                accessToken,
                refreshToken,
                accessExpiresInSeconds,
                0,
                getCurrentEpoch()
        );
    }

    @JsonIgnore
    public static OAuth2Token create(String tokenType, String accessToken, String refreshToken,
            long accessExpiresInSeconds, long refreshExpiresInSeconds) {
        return new OAuth2Token(
                tokenType,
                accessToken,
                refreshToken,
                accessExpiresInSeconds,
                refreshExpiresInSeconds,
                getCurrentEpoch()
        );
    }

    @JsonIgnore
    public static OAuth2Token createBearer(String accessToken, String refreshToken,
            long accessExpiresInSeconds) {
        return create(
                BEARER,
                accessToken,
                refreshToken,
                accessExpiresInSeconds
        );
    }

    @JsonIgnore
    public static OAuth2Token createBearer(String accessToken, String refreshToken,
            long accessExpiresInSeconds, long refreshExpiresInSeconds) {
        return create(
                BEARER,
                accessToken,
                refreshToken,
                accessExpiresInSeconds,
                refreshExpiresInSeconds
        );
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @JsonIgnore
    private static long getCurrentEpoch() {
        return System.currentTimeMillis() / 1000L;
    }

    @JsonIgnore
    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    @JsonIgnore
    public boolean hasAccessExpired() {
        long currentTime = getCurrentEpoch();
        return currentTime >= (issuedAt + expiresInSeconds);
    }

    @JsonIgnore
    private boolean hasRefreshExpired() {
        if (refreshExpiresInSeconds == 0) {  // 0 is considered "not specified"
            return false;
        }
        long currentTime = getCurrentEpoch();
        return currentTime >= (issuedAt + refreshExpiresInSeconds);
    }

    @JsonIgnore
    public boolean isValid() {
        return !hasAccessExpired() && !Strings.isNullOrEmpty(accessToken);
    }

    @JsonIgnore
    public boolean canRefresh() {
        return !hasRefreshExpired() && !Strings.isNullOrEmpty(refreshToken);
    }

    @JsonIgnore
    public boolean isBearer() {
        return !Strings.isNullOrEmpty(tokenType) && BEARER.equalsIgnoreCase(tokenType);
    }

    @JsonIgnore
    public String toAuthorizeHeader() {
        // `Bearer XYZ`
        return String.format(
                "%s %s",
                // Upper case first character.
                tokenType.substring(0, 1).toUpperCase() + tokenType.substring(1).toLowerCase(),
                accessToken);
    }

    // TODO: Remove when logging is not needed
    public long getIssuedAt() {
        return issuedAt;
    }

    // TODO: Remove when logging is not needed
    public boolean hasRefreshExpire() {
        return refreshExpiresInSeconds != 0;   // 0 is considered "not specified"
    }

    // TODO: Remove when logging is not needed
    public long getAccessExpireEpoch() {
        return issuedAt + expiresInSeconds;
    }

    // TODO: Remove when logging is not needed
    public long getRefreshExpireEpoch() {
        return issuedAt + refreshExpiresInSeconds;
    }

    // TODO: Remove when logging is not needed
    public boolean isRefreshNullOrEmpty() {
        return Strings.isNullOrEmpty(refreshToken);
    }
}
