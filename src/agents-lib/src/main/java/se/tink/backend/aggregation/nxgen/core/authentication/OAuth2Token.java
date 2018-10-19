package se.tink.backend.aggregation.nxgen.core.authentication;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OAuth2Token {
    private String tokenType;
    private String accessToken;
    @JsonProperty
    private String refreshToken;
    private long expiresInSeconds;
    private long issuedAt;

    private OAuth2Token(@JsonProperty("tokenType") String tokenType,
            @JsonProperty("accessToken") String accessToken,
            @JsonProperty("refreshToken") String refreshToken,
            @JsonProperty("expiresInSeconds") long expiresInSeconds,
            @JsonProperty("issuedAt") long issuedAt) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresInSeconds = expiresInSeconds;
        this.issuedAt = issuedAt;
    }

    @JsonIgnore
    public static OAuth2Token create(String tokenType, String accessToken, String refreshToken,
            long expiresInSeconds) {
        return new OAuth2Token(
                tokenType,
                accessToken,
                refreshToken,
                expiresInSeconds,
                getCurrentEpoch()
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
        return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    }

    @JsonIgnore
    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    @JsonIgnore
    public boolean hasExpired() {
        long currentTime = getCurrentEpoch();
        return currentTime >= (issuedAt + expiresInSeconds);
    }

    @JsonIgnore
    public boolean isValid() {
        return !hasExpired() && !Strings.isNullOrEmpty(accessToken);
    }

    @JsonIgnore
    public boolean isBearer() {
        return !Strings.isNullOrEmpty(tokenType) && "bearer".equalsIgnoreCase(tokenType);
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
}
