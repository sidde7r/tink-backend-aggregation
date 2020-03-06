package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2MultiTokenBase;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;

@RequiredArgsConstructor
public class MultiTokenFetcher<T extends OAuth2TokenBase, M extends OAuth2MultiTokenBase<T>>
        implements AccessTokenFetcher {

    private final AccessTokenFetchHelper<T> accessTokenFetchHelper;
    private final AccessTokenStorage<M> accessTokenStorage;
    private final AccessCodeStorage accessCodeStorage;
    private final Supplier<M> multiTokenSupplier;

    @Override
    public AccessTokenStatus getAccessTokenStatus() {
        return accessTokenStorage
                .getToken()
                .map(this::getTokenStatus)
                .orElse(AccessTokenStatus.NOT_PRESENT);
    }

    @Override
    public AccessTokenRefreshStatus refreshAccessToken() {
        final M multiToken =
                accessTokenStorage
                        .getTokenFromSession()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Cannot find token in session."));

        return refreshAndSaveRefreshToken(multiToken);
    }

    @Override
    public void retrieveAccessToken() {
        final String[] accessCodes =
                accessCodeStorage
                        .getAccessCodeFromSession()
                        .map(codes -> codes.split(","))
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Cannot find access code in session."));

        final List<T> tokens =
                Arrays.stream(accessCodes)
                        .map(accessTokenFetchHelper::retrieveAuthorizationToken)
                        .collect(Collectors.toList());

        accessTokenStorage.storeToken(createMultiToken(tokens));
    }

    private AccessTokenStatus getTokenStatus(M multiToken) {
        return multiToken.isTokenValid()
                ? AccessTokenStatus.VALID
                : checkIfTokenCanBeRefreshedAndStoreInSessionForRefresh(multiToken);
    }

    private AccessTokenStatus checkIfTokenCanBeRefreshedAndStoreInSessionForRefresh(M multiToken) {
        final boolean canRefreshMultiToken =
                multiToken.getTokens().stream().allMatch(OAuth2TokenBase::canRefresh);

        if (canRefreshMultiToken) {
            accessTokenStorage.storeTokenInSession(multiToken);
            return AccessTokenStatus.EXPIRED;
        }

        return AccessTokenStatus.NOT_PRESENT;
    }

    private AccessTokenRefreshStatus refreshAndSaveRefreshToken(M multiToken) {
        final List<T> tokens = new ArrayList<>();

        for (T token : multiToken.getTokens()) {
            final Optional<T> maybeRefreshedToken =
                    accessTokenFetchHelper.retrieveRefreshedToken(token);

            if (maybeRefreshedToken.isPresent()) {
                tokens.add(maybeRefreshedToken.get());
            } else {
                return AccessTokenRefreshStatus.FAILED;
            }
        }

        accessTokenStorage.rotateToken(createMultiToken(tokens));

        return AccessTokenRefreshStatus.SUCCESS;
    }

    private M createMultiToken(List<T> tokens) {
        final M multiToken = multiTokenSupplier.get();
        multiToken.setTokens(tokens);

        return multiToken;
    }
}
