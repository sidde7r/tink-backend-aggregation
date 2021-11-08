package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.steps;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class RefreshAccessTokenStep implements AuthenticationStep {
    private final LhvApiClient apiClient;
    private final PersistentStorage storage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthorizationException {

        Optional<OAuth2Token> oAuth2Token =
                storage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class);

        if (oAuth2Token.isPresent()) {
            return refreshToken(oAuth2Token.get());
        } else {
            return AuthenticationStepResponse.executeNextStep();
        }
    }

    public AuthenticationStepResponse refreshToken(OAuth2Token token) {
        if (!token.canRefresh()) {
            return AuthenticationStepResponse.executeNextStep();
        }

        Optional<String> refreshToken = token.getRefreshToken();

        if (!refreshToken.isPresent()) {
            return AuthenticationStepResponse.executeNextStep();
        }

        OAuth2Token refreshedOAuth2Token =
                refreshTokenResponse(refreshToken.get()).get().toTinkToken();

        if (!refreshedOAuth2Token.isValid()) {
            return AuthenticationStepResponse.executeNextStep();
        }

        OAuth2Token newToken = refreshedOAuth2Token.updateTokenWithOldToken(token);
        storage.rotateStorageValue(PersistentStorageKeys.OAUTH_2_TOKEN, newToken);

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private Optional<TokenResponse> refreshTokenResponse(String refreshToken) {
        return Optional.ofNullable(apiClient.getRefreshToken(refreshToken));
    }
}
