package se.tink.backend.aggregation.nxgen.controllers.authentication.utils;

import com.google.common.base.Preconditions;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;

/**
 * This class should only be used in Open Banking agents. The intention of the class is to return
 * the expiration date of the token retrieved from the ASPSP (Account Servicing Payment Service
 * Provider). The token in this context is the one that gives access to a PSU's (Payment Services
 * User) financial data (that the PSU has given consent to) up to 90 days. Open Banking standards
 * states that a PSU can give consent to a AISP (Account Information Service Provider) to access the
 * data for up to 90 days.
 *
 * <p>Given that a ASPSP provides the lifetime the token, this lifetime should always be used to get
 * the expiration date. In other cases, a developer should take help from API documentation to get
 * information about the lifetime of the tokens returned by the ASPSP. If the API documentation does
 * not provide any information about the lifetime of the token, we should default to 90 days.
 */
@Slf4j
public class OpenBankingTokenExpirationDateHelper {
    private static final int DEFAULT_LIFETIME = 90;
    private static final TemporalUnit DEFAULT_LIFETIME_UNIT = ChronoUnit.DAYS;

    /**
     * Use this method if the ASPSP doe snot return any information through their API and the API
     * docs does not provide any information about how long the lifetime of the token is.
     *
     * @return expiration date in UTC format, which is 90 days from now.
     */
    public static Date getDefaultExpirationDate() {
        return getExpirationDateFrom(DEFAULT_LIFETIME, DEFAULT_LIFETIME_UNIT);
    }

    /**
     * Use this method if the ASPSP provide some information about how long the lifetime of the
     * token is.
     *
     * @param tokenLifetime - the lifetime of the token
     * @param tokenLifetimeUnit - the unit of the lifetime of the token
     * @exception NullPointerException if tokenLifeTime or tokenLifeTimeUnit not set.
     * @return the expiration date in UTC format, will default to 90 days if any of the parameters
     *     are null.
     */
    public static Date getExpirationDateFrom(
            Integer tokenLifetime, TemporalUnit tokenLifetimeUnit) {
        Preconditions.checkNotNull(
                tokenLifetime,
                "If you don't want to set the tokenLifeTime, use the default implementation.");
        Preconditions.checkNotNull(
                tokenLifetimeUnit,
                "If you don't want to set the tokenLifeTimeUnit, use the default implementation.");

        return Date.from(
                LocalDateTime.now()
                        .plus(tokenLifetime, tokenLifetimeUnit)
                        .atOffset(ZoneOffset.UTC)
                        .toInstant());
    }

    /**
     * Use this method if you want to default the token lifetime to the standard 90 days if the
     * token is not available.
     *
     * @param token - OAuth2Token with information about lifetime of token.
     * @return the expiration date in UTC format, will default to 90 days if any of the parameters
     *     are null.
     */
    public static Date getExpirationDateFromTokenOrDefault(OAuth2Token token) {
        return getExpirationDateFrom(token, DEFAULT_LIFETIME, DEFAULT_LIFETIME_UNIT);
    }

    /**
     * Use this method if you want to default to some other lifetime than the standard 90 days if
     * the token is not available.
     *
     * @param token - OAuth2Token with information about lifetime of token.
     * @param tokenLifetime - the lifetime of the token
     * @param tokenLifetimeUnit - the unit of the lifetime of the token
     * @exception NullPointerException if tokenLifeTime or tokenLifeTimeUnit not set.
     * @return the expiration date in UTC format, will default to 90 days if any of the parameters
     *     are null.
     */
    public static Date getExpirationDateFrom(
            @Nullable OAuth2TokenBase token,
            Integer tokenLifetime,
            TemporalUnit tokenLifetimeUnit) {
        if (token == null) {
            return getExpirationDateFrom(tokenLifetime, tokenLifetimeUnit);
        }

        if (!token.isRefreshTokenExpirationPeriodSpecified()) {
            if (!token.getRefreshToken().isPresent()) {
                // Rewrite the token's expiry to the default value if it does not have a refresh
                // token. This is because some banks give us a token with an expiry of 600 seconds,
                // and no refresh token, even though we asked for longer access.
                long tokenExpiryInSeconds =
                        Duration.of(tokenLifetime, tokenLifetimeUnit).getSeconds();
                long originalExpiryInSeconds = token.getExpiresInSeconds();
                log.info(
                        "Updating the token expiry date to {} seconds, original value: {}, diff: {}.",
                        tokenExpiryInSeconds,
                        originalExpiryInSeconds,
                        tokenExpiryInSeconds - originalExpiryInSeconds);
                token.setExpiresInSeconds(tokenExpiryInSeconds);
            }
            return getExpirationDateFrom(tokenLifetime, tokenLifetimeUnit);
        }

        return new Date(token.getRefreshExpireEpoch() * 1000);
    }
}
