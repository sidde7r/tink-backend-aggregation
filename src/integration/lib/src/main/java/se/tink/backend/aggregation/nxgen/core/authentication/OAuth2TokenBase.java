package se.tink.backend.aggregation.nxgen.core.authentication;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class OAuth2TokenBase {

    static final int REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED = 0;

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private long expiresInSeconds;
    private long refreshExpiresInSeconds;
    private long issuedAt;

    public Optional<String> getOptionalRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    public boolean isAccessTokenNotExpired() {
        return !hasAccessExpired();
    }

    public boolean hasAccessExpired() {
        final long validFor = getValidForSecondsTimeLeft();
        if (validFor > 0) {
            log.info(
                    "Access token is valid for {} seconds (issuedAtEpoch: {}, expiresIn: {})",
                    validFor,
                    issuedAt,
                    expiresInSeconds);
        }
        return validFor <= 0;
    }

    private boolean hasRefreshExpired() {
        if (refreshExpiresInSeconds == REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED) {
            log.warn(
                    "[OAuth2TokenBase] refreshExpiresInSeconds not specified -> assuming optimistically that refreshing access token is possible");
            return false;
        }

        final long currentTime = getCurrentEpoch();
        boolean isRefreshTokenExpired = currentTime >= (issuedAt + refreshExpiresInSeconds);
        log.info("[OAuth2TokenBase] Is refresh token expired: {}", isRefreshTokenExpired);
        return isRefreshTokenExpired;
    }

    public void updateWithOldToken(OAuth2TokenBase oldOAuth2Token) {
        Optional<String> possibleOldRefreshToken = oldOAuth2Token.getOptionalRefreshToken();
        if (!this.getOptionalRefreshToken().isPresent() && possibleOldRefreshToken.isPresent()) {
            String oldRefreshToken = possibleOldRefreshToken.get();
            this.setRefreshToken(oldRefreshToken);
            this.setRefreshExpiresInSeconds(
                    oldOAuth2Token.isRefreshTokenExpirationPeriodSpecified()
                            ? oldOAuth2Token.getRefreshExpireEpoch() - this.getIssuedAt()
                            : REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED);
        }
    }

    public boolean isValid() {
        return !hasAccessExpired() && StringUtils.isNotEmpty(accessToken);
    }

    public boolean canNotRefreshAccessToken() {
        return !canRefresh();
    }

    public boolean canRefresh() {
        Optional<String> maybeRefreshToken = getOptionalRefreshToken();

        if (!maybeRefreshToken.isPresent()) {
            log.warn("[OAuth2TokenBase] Refresh token is missing");
            return false;
        }

        return !hasRefreshExpired();
    }

    public boolean isRefreshTokenExpirationPeriodSpecified() {
        return refreshExpiresInSeconds != REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED;
    }

    public long getAccessExpireEpoch() {
        return getIssuedAt() + getExpiresInSeconds();
    }

    public long getRefreshExpireEpoch() {
        return getIssuedAt() + getRefreshExpiresInSeconds();
    }

    public abstract boolean isTokenTypeValid();

    public long getValidForSecondsTimeLeft() {
        return (issuedAt + expiresInSeconds) - getCurrentEpoch();
    }

    static long getCurrentEpoch() {
        return System.currentTimeMillis() / 1000L;
    }

    public static long extractIssuedAtSeconds(String jwt) {
        return JWTUtils.extractIssuedAtSeconds(jwt, getCurrentEpoch());
    }

    public static long calculateExpiresInSeconds(String jwt) {
        return JWTUtils.calculateExpiresInSeconds(jwt, REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED);
    }
}
