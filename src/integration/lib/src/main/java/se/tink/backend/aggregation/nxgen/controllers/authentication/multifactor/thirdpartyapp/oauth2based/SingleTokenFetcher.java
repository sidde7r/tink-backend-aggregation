package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;

@RequiredArgsConstructor
public class SingleTokenFetcher<T extends OAuth2TokenBase> implements AccessTokenFetcher {

    private final AccessTokenFetchHelper<T> accessTokenFetchHelper;
    private final AccessTokenStorage<T> accessTokenStorage;
    private final AccessCodeStorage accessCodeStorage;

    @Override
    public AccessTokenStatus getAccessTokenStatus() {
        return accessTokenStorage
                .getToken()
                .map(this::getTokenStatus)
                .orElse(AccessTokenStatus.NOT_PRESENT);
    }

    @Override
    public AccessTokenRefreshStatus refreshAccessToken() {
        final T token =
                accessTokenStorage
                        .getTokenFromSession()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Cannot find token in session."));

        return refreshAndSaveRefreshToken(token);
    }

    @Override
    public void retrieveAccessToken() {
        final String accessCode =
                accessCodeStorage
                        .getAccessCodeFromSession()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Cannot find access code in session."));

        final T token = accessTokenFetchHelper.retrieveAuthorizationToken(accessCode);

        accessTokenStorage.storeToken(token);
    }

    private AccessTokenStatus getTokenStatus(T token) {
        return token.isValid()
                ? AccessTokenStatus.VALID
                : checkIfTokenCanBeRefreshedAndStoreInSession(token);
    }

    private AccessTokenStatus checkIfTokenCanBeRefreshedAndStoreInSession(T token) {
        if (token.canRefresh()) {
            accessTokenStorage.storeTokenInSession(token);
            return AccessTokenStatus.EXPIRED;
        }

        return AccessTokenStatus.NOT_PRESENT;
    }

    private AccessTokenRefreshStatus refreshAndSaveRefreshToken(T token) {
        final Optional<T> maybeRefreshedToken =
                accessTokenFetchHelper.retrieveRefreshedToken(token);

        if (maybeRefreshedToken.isPresent()) {
            accessTokenStorage.rotateToken(maybeRefreshedToken.get());
            return AccessTokenRefreshStatus.SUCCESS;
        }

        return AccessTokenRefreshStatus.FAILED;
    }
}
