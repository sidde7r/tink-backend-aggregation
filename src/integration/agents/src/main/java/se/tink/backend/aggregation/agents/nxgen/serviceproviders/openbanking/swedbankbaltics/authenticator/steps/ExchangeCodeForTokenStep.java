package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import java.lang.invoke.MethodHandles;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class ExchangeCodeForTokenStep implements AuthenticationStep {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
            GenericResponse errorResponse = e.getResponse().getBody(GenericResponse.class);
            logger.warn(
                    String.format("Can exchange token for code. Got error (%s)", errorResponse));
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }

    @Override
    public String getIdentifier() {
        return "exchange_code_for_token_step";
    }
}
