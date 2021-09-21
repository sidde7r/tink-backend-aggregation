package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.helper;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class SCAAuthenticationHelper {
    private final SwedbankBalticsApiClient apiClient;
    private final StepDataStorage stepDataStorage;
    private final PersistentStorage persistentStorage;
    private final SwedbankBalticsAuthenticator authenticator;

    public void scaAuthentication(ConsentResponse consentResponse) {

        String url = consentResponse.getLinks().getHrefEntity().getHref();
        AuthenticationResponse authResponse = apiClient.authorizeConsent(url);

        Uninterruptibles.sleepUninterruptibly(
                TimeValues.SCA_STATUS_POLL_DELAY, TimeUnit.MILLISECONDS);

        // we send challenge code that we received in response to user, user have to compare
        // this code and code in SMART_ID
        authenticator.displayChallengeCodeToUser(authResponse.getChallengeData().getCode());

        for (int i = 0; i < TimeValues.SCA_STATUS_POLL_MAX_ATTEMPTS; i++) {
            String status = apiClient.getScaStatus(authResponse.getCollectAuthUri());

            switch (status.toLowerCase()) {
                case AuthStatus.RECEIVED:
                case AuthStatus.STARTED:
                    log.info("Waiting for authentication");
                    break;
                case AuthStatus.FINALIZED:
                    persistentStorage.put(
                            SwedbankConstants.StorageKeys.CONSENT, consentResponse.getConsentId());
                    return;
                case AuthStatus.FAILED:
                    throw AuthorizationError.UNAUTHORIZED.exception();
                default:
                    log.warn("Unknown status {}", status);
                    throw AuthorizationError.UNAUTHORIZED.exception();
            }

            Uninterruptibles.sleepUninterruptibly(
                    TimeValues.SCA_STATUS_POLL_FREQUENCY, TimeUnit.MILLISECONDS);
        }
        log.warn("Timeout");
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }
}
