package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OAuth2Token {
    // Update this value to void previous tokens (for changes that break backward compatability).
    private static final String CURRENT_VERSION = "o2_1.0";

    private String version;
    private String tokenType;
    private String accessToken;
    @JsonProperty
    private String refreshToken;
    private long expiresInSeconds;
    private long issuedAt;

    private OAuth2Token(@JsonProperty("version") String version,
            @JsonProperty("tokenType") String tokenType,
            @JsonProperty("accessToken") String accessToken,
            @JsonProperty("refreshToken") String refreshToken,
            @JsonProperty("expiresInSeconds") long expiresInSeconds,
            @JsonProperty("issuedAt") long issuedAt) {
        this.version = version;
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresInSeconds = expiresInSeconds;
        this.issuedAt = issuedAt;
    }

    @JsonIgnore
    public static OAuth2Token create(String tokenType, String accessToken, String refreshToken, long expiresInSeconds) {
        return new OAuth2Token(
                CURRENT_VERSION,
                tokenType,
                accessToken,
                refreshToken,
                expiresInSeconds,
                getCurrentEpoch()
        );
    }

    @JsonIgnore
    public static OAuth2Token create(OAuth2TokenResponse response) {
        return create(
                response.getTokenType(),
                response.getAccessToken(),
                response.getRefreshToken().orElse(null),
                response.getExpiresInSeconds()
        );
    }

    @JsonIgnore
    private static long getCurrentEpoch() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @JsonIgnore
    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    @JsonIgnore
    private boolean hasExpired() {
        long currentTime = getCurrentEpoch();
        return currentTime >= (issuedAt + expiresInSeconds);
    }

    @JsonIgnore
    private boolean isCurrentVersion() {
        return Objects.equals(version, CURRENT_VERSION);
    }

    @JsonIgnore
    public boolean isValid() {
        return isCurrentVersion() && !hasExpired() && !Strings.isNullOrEmpty(accessToken);
    }

    @JsonIgnore
    public boolean isBearer() {
        return !Strings.isNullOrEmpty(tokenType) && "bearer".equalsIgnoreCase(tokenType);
    }
}
