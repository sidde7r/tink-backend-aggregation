package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class GetConsentForAllAccountsStep implements AuthenticationStep {

    private final SwedbankBalticsApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final StepDataStorage stepDataStorage;
    private final User user;

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

        if (!user.isAvailableForInteraction()) {
            throw new IllegalStateException("Can not renew consent since the user is not present");
        }

        ConsentResponse consentResponse = apiClient.getConsentAllAccounts();

        if (ConsentStatus.VALID.equalsIgnoreCase(consentResponse.getConsentStatus())) {
            persistentStorage.put(
                    SwedbankConstants.StorageKeys.CONSENT, consentResponse.getConsentId());
            return AuthenticationStepResponse.executeStepWithId(Steps.GET_ALL_ACCOUNTS_STEP);
        } else {
            stepDataStorage.putConsentResponseForAllAccounts(consentResponse);
            return AuthenticationStepResponse.executeStepWithId(Steps.ALL_ACCOUNTS_CONSENT_AUTH);
        }
    }

    @Override
    public String getIdentifier() {
        return Steps.GET_CONSENT_FOR_ALL_ACCOUNTS_STEP;
    }
}
