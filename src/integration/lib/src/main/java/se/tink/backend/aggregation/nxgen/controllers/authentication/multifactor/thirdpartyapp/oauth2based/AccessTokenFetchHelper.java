package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;

@RequiredArgsConstructor
@Slf4j
public class AccessTokenFetchHelper<T extends OAuth2TokenBase> {

    public static final int DEFAULT_TOKEN_LIFETIME = 90;
    public static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;

    private final AccessTokenProvider<T> tokenProvider;
    private final Credentials credentials;
    private final TokenLifeTime tokenLifeTime;

    T retrieveAuthorizationToken(String accessCode) {
        final T token = tokenProvider.exchangeAuthorizationCode(accessCode);

        validateToken(token);

        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        token, tokenLifeTime.getLifetime(), tokenLifeTime.getUnit()));

        return token;
    }

    Optional<T> retrieveRefreshedToken(T token) {
        return token.getRefreshToken()
                .flatMap(tokenProvider::refreshAccessToken)
                .filter(OAuth2TokenBase::isValid)
                .map(refreshedToken -> updateRefreshToken(refreshedToken, token));
    }

    private T updateRefreshToken(T refreshedToken, T oldToken) {
        if (refreshedToken.hasRefreshExpire()) {
            credentials.setSessionExpiryDate(
                    OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                            refreshedToken, tokenLifeTime.getLifetime(), tokenLifeTime.getUnit()));
        }

        refreshedToken.updateWithOldToken(oldToken);

        return refreshedToken;
    }

    private void validateToken(OAuth2TokenBase token) {
        if (!token.isValid()) {
            throw new IllegalArgumentException("Invalid access token.");
        }

        if (!token.isTokenTypeValid()) {
            throw new IllegalArgumentException(
                    String.format("Unknown token type '%s'.", token.getTokenType()));
        }
    }
}
