package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.steps;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.PollValues;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
@Slf4j
public class CreateNewConsentStep implements AuthenticationStep {

    private final LhvApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthorizationException, AuthenticationException {

        AccountSummaryResponse accountSummaryList = apiClient.getAccountSummary();
        ConsentResponse consentResponse = apiClient.getConsent(accountSummaryList);

        URL url = new URL(consentResponse.getLinks().getScaRedirect().getHref());

        poll(consentResponse.getConsentId(), url);

        persistentStorage.put(StorageKeys.USER_CONSENT_ID, consentResponse.getConsentId());

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private void poll(String consentId, URL url)
            throws AuthenticationException, AuthorizationException {
        ConsentResponse response;

        // first attempt will open consent page
        response = apiClient.getConsentStatus(consentId);
        if (response.getConsentStatus().equals(ConsentStatus.RECEIVED)) {
            supplementalInformationHelper.openThirdPartyApp(
                    ThirdPartyAppAuthenticationPayload.of(url));
        }

        for (int i = 0; i < PollValues.SMART_ID_POLL_MAX_ATTEMPTS; i++) {
            response = apiClient.getConsentStatus(consentId);
            switch (response.getConsentStatus()) {
                case ConsentStatus.RECEIVED:
                    break;
                case ConsentStatus.VALID:
                    return;
                default:
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
            }

            Uninterruptibles.sleepUninterruptibly(
                    PollValues.SMART_ID_POLL_FREQUENCY, TimeUnit.MILLISECONDS);
        }

        throw ThirdPartyAppError.TIMED_OUT.exception();
    }
}
