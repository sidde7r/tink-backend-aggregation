package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Token.DEFAULT_TOKEN_LIFETIME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Token.DEFAULT_TOKEN_LIFETIME_UNIT;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
final class OpenIdAccessTokenRefresher implements AccessTokenRefresher {

    private final OpenIdApiClient openIdApiClient;
    private final Credentials credentials;

    @Override
    public OAuth2Token refresh(OAuth2Token oAuth2Token) throws SessionException {
        log.info(
                "[OpenIdAccessTokenRefresher] Trying to refresh access token. "
                        + "Issued: [{}] "
                        + "Access Expires: [{}] "
                        + "Has Refresh Token: [{}] "
                        + "Refresh Token Expires: [{}]",
                LocalDateTime.ofEpochSecond(oAuth2Token.getIssuedAt(), 0, ZoneOffset.UTC),
                LocalDateTime.ofEpochSecond(oAuth2Token.getAccessExpireEpoch(), 0, ZoneOffset.UTC),
                oAuth2Token.isRefreshNullOrEmpty(),
                oAuth2Token.isRefreshTokenExpirationPeriodSpecified()
                        ? LocalDateTime.ofEpochSecond(
                                oAuth2Token.getRefreshExpireEpoch(), 0, ZoneOffset.UTC)
                        : "N/A");

        Optional<String> optionalRefreshToken = oAuth2Token.getOptionalRefreshToken();
        if (!optionalRefreshToken.isPresent()) {
            log.error(
                    "[OpenIdAccessTokenRefresher] Refresh token is not present. "
                            + "Access token refresh failed.");
            throw SessionError.SESSION_EXPIRED.exception();
        }
        String refreshToken = optionalRefreshToken.get();

        try {
            OAuth2Token refreshedOAuth2Token =
                    openIdApiClient.refreshAccessToken(refreshToken, ClientMode.ACCOUNTS);

            if (!refreshedOAuth2Token.isValid()) {
                log.warn(
                        "[OpenIdAccessTokenRefresher] Access token refreshed, but it is invalid. "
                                + "Expiring the session.");
                throw SessionError.SESSION_EXPIRED.exception();
            }

            if (refreshedOAuth2Token.isRefreshTokenExpirationPeriodSpecified()) {
                credentials.setSessionExpiryDate(
                        OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                                refreshedOAuth2Token,
                                DEFAULT_TOKEN_LIFETIME,
                                DEFAULT_TOKEN_LIFETIME_UNIT));
            }

            oAuth2Token = refreshedOAuth2Token.updateTokenWithOldToken(oAuth2Token);

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() >= 500) {
                log.warn(
                        "[OpenIdAccessTokenRefresher] Bank side error (status code {}) during "
                                + "refreshing token",
                        e.getResponse().getStatus());
                throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
            }
            log.error(
                    "[OpenIdAccessTokenRefresher] Access token refresh failed: {}",
                    e.getResponse().getBody(String.class));

            throw SessionError.SESSION_EXPIRED.exception();
        }

        log.info(
                "[OpenIdAccessTokenRefresher] Token refreshed successfully. "
                        + "New token - Access Expires: [{}] "
                        + "Has Refresh Token: [{}] "
                        + "Refresh Expires: [{}]",
                LocalDateTime.ofEpochSecond(oAuth2Token.getAccessExpireEpoch(), 0, ZoneOffset.UTC),
                oAuth2Token.isRefreshNullOrEmpty(),
                oAuth2Token.isRefreshTokenExpirationPeriodSpecified()
                        ? LocalDateTime.ofEpochSecond(
                                oAuth2Token.getRefreshExpireEpoch(), 0, ZoneOffset.UTC)
                        : "N/A");
        return oAuth2Token;
    }
}
