package se.tink.backend.aggregation.nxgen.core.authentication;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class OAuth2Token extends OAuth2TokenBase {

    private static final String BEARER = "bearer";

    public OAuth2Token(
            String tokenType,
            String accessToken,
            String refreshToken,
            String idToken,
            long expiresInSeconds,
            long refreshExpiresInSeconds,
            long issuedAt) {
        super(
                tokenType,
                accessToken,
                refreshToken,
                idToken,
                expiresInSeconds,
                refreshExpiresInSeconds,
                issuedAt);
    }

    public static OAuth2Token create(
            String tokenType,
            String accessToken,
            String refreshToken,
            long accessExpiresInSeconds) {
        return new OAuth2Token(
                tokenType,
                accessToken,
                refreshToken,
                null,
                accessExpiresInSeconds,
                OAuth2TokenBase.REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED,
                getCurrentEpoch());
    }

    public static OAuth2Token create(
            String tokenType,
            String accessToken,
            String refreshToken,
            long accessExpiresInSeconds,
            long refreshExpiresInSeconds) {
        return new OAuth2Token(
                tokenType,
                accessToken,
                refreshToken,
                null,
                accessExpiresInSeconds,
                refreshExpiresInSeconds,
                getCurrentEpoch());
    }

    public static OAuth2Token create(
            String tokenType,
            String accessToken,
            String refreshToken,
            String idToken,
            long accessExpiresInSeconds,
            long refreshExpiresInSeconds) {
        return new OAuth2Token(
                tokenType,
                accessToken,
                refreshToken,
                idToken,
                accessExpiresInSeconds,
                refreshExpiresInSeconds,
                getCurrentEpoch());
    }

    public static OAuth2Token createBearer(
            String accessToken, String refreshToken, long accessExpiresInSeconds) {
        return create(BEARER, accessToken, refreshToken, accessExpiresInSeconds);
    }

    public void updateAccessToken(String accessToken, long accessExpiresInSeconds) {
        long currentTime = getCurrentEpoch();
        setAccessToken(accessToken);
        setExpiresInSeconds(accessExpiresInSeconds);
        setRefreshExpiresInSeconds(getIssuedAt() + getRefreshExpiresInSeconds() - currentTime);
        setIssuedAt(currentTime);
    }

    public boolean isBearer() {
        return StringUtils.isNotEmpty(getTokenType()) && BEARER.equalsIgnoreCase(getTokenType());
    }

    @Override
    public boolean isTokenTypeValid() {
        return isBearer();
    }

    public String toAuthorizeHeader() {
        // `Bearer XYZ`
        return String.format(
                "%s %s",
                getTokenType().substring(0, 1).toUpperCase()
                        + getTokenType().substring(1).toLowerCase(),
                getAccessToken());
    }

    public OAuth2Token updateTokenWithOldToken(OAuth2Token oldOAuth2Token) {
        this.updateWithOldToken(oldOAuth2Token);

        return this;
    }

    public boolean isRefreshNullOrEmpty() {
        return getRefreshToken().isPresent();
    }
}
