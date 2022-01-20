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
    static final int FIVE_MINUTE_TIME_LIMIT = 300;

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private long expiresInSeconds;
    private long refreshExpiresInSeconds;
    private long issuedAt;

    /**
     * getTokenMinimumAllowedSecondsValidFor() is used to fetch a minimum time limit before the
     * access token has to be renewed. This limit will either be five minutes, or it will be 10% of
     * the tokens initial lifespan. The last case is added to prevent deadlocks with access tokens
     * with a lifespan shorter than five minutes.
     */
    public double getTokenMinimumAllowedSecondsValidFor() {
        return Math.min(expiresInSeconds * 0.1, FIVE_MINUTE_TIME_LIMIT);
    }

    public Optional<String> getOptionalRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    public boolean canUseAccessToken() {
        final long validFor = getValidForSecondsTimeLeft(expiresInSeconds);

        if (validFor > getTokenMinimumAllowedSecondsValidFor()) {
            log.info(
                    "Access token can be used for {} seconds (issuedAtEpoch: {}, expiresIn: {})",
                    validFor,
                    issuedAt,
                    expiresInSeconds);
        }
        return validFor > getTokenMinimumAllowedSecondsValidFor();
    }

    private boolean hasRefreshTokenExpired() {
        if (refreshExpiresInSeconds == REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED) {
            log.warn(
                    "[OAuth2TokenBase] refreshExpiresInSeconds not specified -> assuming optimistically that refreshing access token is possible");
            return false;
        }

        boolean isRefreshTokenExpired = getValidForSecondsTimeLeft(refreshExpiresInSeconds) <= 0;
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
        return canUseAccessToken() && StringUtils.isNotEmpty(accessToken);
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

        return !hasRefreshTokenExpired();
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

    public long getValidForSecondsTimeLeft(Long tokenExpiresInSeconds) {
        return (issuedAt + tokenExpiresInSeconds) - getCurrentEpoch();
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
