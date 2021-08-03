package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class GetConsentForAllAccountsStep implements AuthenticationStep {

    private final SwedbankBalticsApiClient apiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        try {
            if (apiClient.isConsentValid()) {
                return AuthenticationStepResponse.authenticationSucceeded();
            }
        } catch (HttpResponseException e) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(e.getMessage());
        }

        persistentStorage.put(
                SwedbankConstants.StorageKeys.CONSENT,
                apiClient.getConsentAllAccounts().getConsentId());
        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public String getIdentifier() {
        return Steps.GET_CONSENT_FOR_ALL_ACCOUNTS_STEP;
    }
}
