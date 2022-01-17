package se.tink.backend.aggregation.nxgen.core.authentication;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.http.header.AuthorizationHeader;

@Slf4j
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class OAuth2Token extends OAuth2TokenBase implements AuthorizationHeader {

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

    public static OAuth2Token createBearer(
            String accessToken,
            String refreshToken,
            String idToken,
            long expiresInSeconds,
            long refreshExpiresInSeconds,
            long issuedAt) {
        return new OAuth2Token(
                BEARER,
                accessToken,
                refreshToken,
                idToken,
                expiresInSeconds,
                refreshExpiresInSeconds,
                issuedAt);
    }

    public static OAuth2Token createBearer(
            String accessToken,
            String refreshToken,
            String idToken,
            long expiresInSeconds,
            long issuedAt) {
        return new OAuth2Token(
                BEARER,
                accessToken,
                refreshToken,
                idToken,
                expiresInSeconds,
                REFRESH_TOKEN_EXPIRES_NOT_SPECIFIED,
                issuedAt);
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

    @Override
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
        return !getOptionalRefreshToken().isPresent();
    }

    /**
     * Useful for debugging (e.g token mismatch between refreshes). Will hide the outcome even if
     * masking is off. It is null and exception safe.
     *
     * @param logMasker
     * @return masked token details
     */
    public String toMaskedString(LogMasker logMasker) {
        try {
            logMasker.addNewSensitiveValuesToMasker(
                    ImmutableSet.of(
                            Strings.nullToEmpty(getAccessToken()),
                            Strings.nullToEmpty(getRefreshToken()),
                            Strings.nullToEmpty(getIdToken())));
            return toGuardedNullSafeMaskedString(
                    logMasker,
                    "OAuth2Token{"
                            + "tokenType = "
                            + toNullSafeMaskedString(logMasker, getTokenType())
                            + ", accessToken = "
                            + toNullSafeMaskedString(logMasker, getAccessToken())
                            + ", refreshToken = "
                            + toNullSafeMaskedString(logMasker, getRefreshToken())
                            + ", idToken = "
                            + toNullSafeMaskedString(logMasker, getIdToken())
                            + ", expiresInSeconds = "
                            + getExpiresInSeconds()
                            + " ["
                            + toLocalDateTime(getExpiresInSeconds())
                            + "]"
                            + ", refreshExpiresInSeconds = "
                            + getRefreshExpiresInSeconds()
                            + " ["
                            + toLocalDateTime(getRefreshExpiresInSeconds())
                            + "]"
                            + ", issuedAt = "
                            + getIssuedAt()
                            + " ["
                            + toLocalDateTime(getIssuedAt())
                            + "]"
                            + '}');
        } catch (Exception e) {
            String maskingFailedMsg = "Masking token failed";
            log.error(maskingFailedMsg, e);
            return maskingFailedMsg;
        }
    }

    private String toGuardedNullSafeMaskedString(LogMasker logMasker, String value) {
        String masked = toNullSafeMaskedString(logMasker, value);
        if (!StringUtils.contains(masked, "HASHED")) {
            return "Masking not applied correctly. Hiding the output.";
        }
        return masked;
    }

    private String toNullSafeMaskedString(LogMasker logMasker, String value) {
        return Optional.ofNullable(value).map(logMasker::mask).orElse("null");
    }

    private LocalDateTime toLocalDateTime(long seconds) {
        return LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC);
    }
}
