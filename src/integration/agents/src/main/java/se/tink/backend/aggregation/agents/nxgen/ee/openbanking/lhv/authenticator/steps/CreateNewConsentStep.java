package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.steps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
@Slf4j
public class CreateNewConsentStep implements AuthenticationStep {

    private final LhvApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final StrongAuthenticationState strongAuthenticationState;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthorizationException, AuthenticationException {

        AccountSummaryResponse accountSummaryList = apiClient.getAccountSummary();
        ConsentResponse consentResponse =
                apiClient.getConsent(accountSummaryList, strongAuthenticationState.getState());

        if (consentResponse.getConsentStatus().equalsIgnoreCase(ConsentStatus.RECEIVED)) {
            persistentStorage.put(StorageKeys.USER_CONSENT_ID, consentResponse.getConsentId());
            persistentStorage.put(
                    StorageKeys.CONSENT_STATUS_URL,
                    consentResponse.getLinks().getScaRedirect().getHref());
            return AuthenticationStepResponse.executeNextStep();
        }
        throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(
                "Authorization process cancelled or bad credentials provided.");
    }
}
