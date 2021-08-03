package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class ExchangeCodeForTokenStep implements AuthenticationStep {

    private final SwedbankBalticsApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final StepDataStorage stepDataStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        OAuth2Token accessToken = exchangeCodeForToken(stepDataStorage.getAuthCode());

        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, accessToken);
        request.getCredentials()
                .setSessionExpiryDate(
                        OpenBankingTokenExpirationDateHelper.getExpirationDateFromTokenOrDefault(
                                accessToken));

        return AuthenticationStepResponse.executeNextStep();
    }

    private OAuth2Token exchangeCodeForToken(String code) {
        try {
            return apiClient.exchangeCodeForToken(code);
        } catch (HttpResponseException e) {
            log.warn(
                    "Can exchange token for code. Status code {}, body {}",
                    e.getResponse().getStatus(),
                    e.getResponse().getBody(String.class));
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }

    @Override
    public String getIdentifier() {
        return Steps.EXCHANGE_CODE_FOR_TOKEN_STEP;
    }
}
