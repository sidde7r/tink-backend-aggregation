package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.steps;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.DecoupledTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class RefreshAccessTokenStep implements AuthenticationStep {
    private final SebBalticsApiClient apiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        Optional<OAuth2Token> oAuth2Token =
                persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class);

        if (oAuth2Token.isPresent()) {
            return refreshToken(oAuth2Token.get());
        } else {
            return AuthenticationStepResponse.executeNextStep();
        }
    }

    public AuthenticationStepResponse refreshToken(OAuth2Token oAuth2Token) {
        if (!oAuth2Token.canRefresh()) {
            return AuthenticationStepResponse.executeNextStep();
        }

        Optional<String> refreshToken = oAuth2Token.getRefreshToken();

        if (!refreshToken.isPresent()) {
            return AuthenticationStepResponse.executeNextStep();
        }

        Optional<TokenResponse> refreshedOAuth2TokenResponse =
                refreshAccessTokenResponse(refreshToken.get());

        if (!refreshedOAuth2TokenResponse.isPresent()) {
            return AuthenticationStepResponse.executeNextStep();
        }

        OAuth2Token refreshedOAuth2Token = refreshedOAuth2TokenResponse.get().toTinkToken();

        if (!refreshedOAuth2Token.isValid()) {
            return AuthenticationStepResponse.executeNextStep();
        }

        oAuth2Token = refreshedOAuth2Token.updateTokenWithOldToken(oAuth2Token);

        // Store the new access token on the persistent storage again.
        persistentStorage.rotateStorageValue(PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);

        // if user consent is invalid, go to the consent creation step
        if (!apiClient.isConsentValid()) {
            return AuthenticationStepResponse.executeStepWithId("create_new_consent_step");
        }

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private Optional<TokenResponse> refreshAccessTokenResponse(String refreshToken) {

        return Optional.ofNullable(
                apiClient.getDecoupledToken(
                        DecoupledTokenRequest.builder()
                                .grantType("refresh_token")
                                .refreshToken(refreshToken)
                                .build()));
    }
}
