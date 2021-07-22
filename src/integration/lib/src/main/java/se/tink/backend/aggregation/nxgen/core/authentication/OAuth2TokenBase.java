package se.tink.backend.aggregation.nxgen.core.authentication;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class OAuth2TokenBase {

    static final int REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED = 0;
    private static final Logger logger = LoggerFactory.getLogger(OAuth2TokenBase.class);

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private long expiresInSeconds;
    private long refreshExpiresInSeconds;
    private long issuedAt;

    public Optional<String> getRefreshToken() { // left for compatibility with the rest of the code
        return Optional.ofNullable(refreshToken);
    }

    public boolean hasAccessExpired() {
        final long validFor = getValidForSecondsTimeLeft();
        if (validFor > 0) {
            String logMessage =
                    "Access token is valid for %s seconds (issuedAtEpoch: %s, expiresIn: %s)";
            logger.info(String.format(logMessage, validFor, issuedAt, expiresInSeconds));
        }
        return validFor <= 0;
    }

    private boolean hasRefreshExpired() {
        if (refreshExpiresInSeconds == REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED) {
            return false;
        }

        final long currentTime = getCurrentEpoch();
        return currentTime >= (issuedAt + refreshExpiresInSeconds);
    }

    public void updateWithOldToken(OAuth2TokenBase oldOAuth2Token) {
        Optional<String> possibleOldRefreshToken = oldOAuth2Token.getRefreshToken();
        if (!this.getRefreshToken().isPresent() && possibleOldRefreshToken.isPresent()) {
            String oldRefreshToken = possibleOldRefreshToken.get();
            this.setRefreshToken(oldRefreshToken);
            this.setRefreshExpiresInSeconds(
                    oldOAuth2Token.hasRefreshExpire()
                            ? oldOAuth2Token.getRefreshExpireEpoch() - this.getIssuedAt()
                            : REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED);
        }
    }

    public boolean isValid() {
        return !hasAccessExpired() && StringUtils.isNotEmpty(accessToken);
    }

    public boolean canRefresh() {
        return getRefreshToken().isPresent() && !hasRefreshExpired();
    }

    public boolean hasRefreshExpire() {
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
