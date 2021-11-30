package se.tink.agent.sdk.models.authentication;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.base.Strings;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

// TODO: Implement better, steppable, builder (?)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@EqualsAndHashCode
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RefreshableAccessToken {
    public static final String STORAGE_KEY = "oauth2_access_token";
    static final long REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED = 0;
    private final String tokenType;
    private final String accessToken;
    private final String refreshToken;
    private final String idToken;
    private final long expiresInSeconds;
    private final long refreshExpiresInSeconds;
    private final long issuedAt;

    public boolean isAccessTokenValid() {
        if (Strings.isNullOrEmpty(this.accessToken)) {
            return false;
        }

        long accessTokenValidForSecondsTimeLeft = this.getAccessTokenValidForSecondsTimeLeft();
        return accessTokenValidForSecondsTimeLeft <= 0;
    }

    public boolean isRefreshTokenValid() {
        if (Strings.isNullOrEmpty(this.getRefreshToken().orElse(null))) {
            return false;
        }

        return this.getRefreshExpiresInSeconds()
                .map(
                        expiresInSeconds ->
                                getCurrentEpoch() >= (this.getIssuedAt() + expiresInSeconds))
                // The refresh token is deemed valid if there is no `refreshExpiresInSeconds`.
                .orElse(true);
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    public Optional<Long> getRefreshExpiresInSeconds() {
        if (this.refreshExpiresInSeconds == REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED) {
            return Optional.empty();
        }
        return Optional.of(this.refreshExpiresInSeconds);
    }

    public Optional<Long> getRefreshExpireEpoch() {
        return this.getRefreshExpiresInSeconds()
                .map(expiresInSeconds -> getIssuedAt() - expiresInSeconds);
    }

    public RefreshableAccessToken updateWith(RefreshableAccessToken otherToken) {
        // Use the other token's refreshToken & expiry (updated) if this token doesn't contain one.
        if (!this.getRefreshToken().isPresent() && otherToken.getRefreshToken().isPresent()) {
            Long refreshTokenExpires =
                    otherToken
                            .getRefreshExpireEpoch()
                            .map(expireEpoch -> expireEpoch - this.getIssuedAt())
                            .orElse(REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED);

            return this.toBuilder()
                    .refreshToken(otherToken.getRefreshToken().get())
                    .refreshExpiresInSeconds(refreshTokenExpires)
                    .build();
        }

        return this;
    }

    public long getAccessTokenValidForSecondsTimeLeft() {
        return (issuedAt + expiresInSeconds) - getCurrentEpoch();
    }

    static long getCurrentEpoch() {
        return System.currentTimeMillis() / 1000L;
    }
}
