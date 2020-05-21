package se.tink.backend.aggregation.nxgen.core.authentication;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class OAuth2TokenBase {

    static final int REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED = 0;

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private long expiresInSeconds;
    private long refreshExpiresInSeconds;
    private long issuedAt;

    public Optional<String> getRefreshToken() { // left for compatibility with the rest of the code
        return Optional.ofNullable(refreshToken);
    }

    public boolean hasAccessExpired() {
        final long currentTime = getCurrentEpoch();
        return currentTime >= (issuedAt + expiresInSeconds);
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
            this.setRefreshToken(possibleOldRefreshToken.get());
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
        return !hasRefreshExpired() && StringUtils.isNotEmpty(refreshToken);
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

    static long getCurrentEpoch() {
        return System.currentTimeMillis() / 1000L;
    }
}
