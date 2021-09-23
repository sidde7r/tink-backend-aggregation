package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.steps;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class CheckIfAccessTokenIsValidStep implements AuthenticationStep {
    private final LhvApiClient apiClient;
    private final PersistentStorage storage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final AccessTokenStatus accessTokenStatus = getStatusFromStorage();

        if (accessTokenStatus == AccessTokenStatus.VALID && apiClient.isConsentValid()) {
            return AuthenticationStepResponse.authenticationSucceeded();
        } else {
            return AuthenticationStepResponse.executeNextStep();
        }
    }

    private AccessTokenStatus getStatusFromStorage() {
        Optional<OAuth2Token> oAuth2Token =
                storage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class);
        return oAuth2Token
                .map(CheckIfAccessTokenIsValidStep::getTokenStatus)
                .orElse(AccessTokenStatus.NOT_PRESENT);
    }

    private static AccessTokenStatus getTokenStatus(OAuth2Token token) {
        return token.isValid() ? AccessTokenStatus.VALID : AccessTokenStatus.EXPIRED;
    }
}
