package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class GetDetailedConsentStep implements AuthenticationStep {

    private final SwedbankBalticsApiClient apiClient;
    private final StepDataStorage stepDataStorage;
    private final PersistentStorage persistentStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        Optional<FetchAccountResponse> fetchAccountResponse = stepDataStorage.getAccountResponse();

        if (fetchAccountResponse.isPresent()) {

            ConsentResponse consentResponse =
                    apiClient.getConsentAccountDetails(fetchAccountResponse.get().getIbanList());

            if (ConsentStatus.VALID.equalsIgnoreCase(consentResponse.getConsentStatus())) {
                persistentStorage.put(
                        SwedbankConstants.StorageKeys.CONSENT, consentResponse.getConsentId());
                return AuthenticationStepResponse.authenticationSucceeded();
            } else {
                stepDataStorage.putConsentResponse(consentResponse);
                return AuthenticationStepResponse.executeNextStep();
            }
        } else {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }

    @Override
    public String getIdentifier() {
        return SwedbankBalticsConstants.GET_DETAILED_CONSENT_STEP;
    }
}
