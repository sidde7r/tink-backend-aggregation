package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class RefreshAccessTokenStep implements AuthenticationStep {

    private final SwedbankBalticsApiClient apiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        Optional<OAuth2Token> token =
                persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class);

        if (token.isPresent() && token.get().canRefresh()) {
            Optional<String> refreshToken = token.get().getRefreshToken();

            if (refreshToken.isPresent()) {
                try {
                    OAuth2Token newToken = apiClient.refreshToken(refreshToken.get());
                    persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, newToken);

                    request.getCredentials()
                            .setSessionExpiryDate(
                                    OpenBankingTokenExpirationDateHelper
                                            .getExpirationDateFromTokenOrDefault(newToken));
                } catch (HttpResponseException e) {
                    return AuthenticationStepResponse.executeNextStep();
                }

                return AuthenticationStepResponse.executeStepWithId(
                        Steps.GET_CONSENT_FOR_ALL_ACCOUNTS_STEP);
            } else {
                throw new IllegalStateException("Missing refresh token");
            }
        } else {
            return AuthenticationStepResponse.executeNextStep();
        }
    }

    @Override
    public String getIdentifier() {
        return Steps.REFRESH_ACCESS_TOKEN_STEP;
    }
}
