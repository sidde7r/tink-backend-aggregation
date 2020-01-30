package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class SibsThirdPartyAuthenticationStep {

    private static final Logger logger =
            LoggerFactory.getLogger(SibsThirdPartyAuthenticationStep.class);

    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 10;

    private final Retryer<ConsentStatus> consentStatusRetryer;

    private final StrongAuthenticationState strongAuthenticationState;
    private final ConsentManager consentManager;
    private final SibsAuthenticator authenticator;

    private SibsThirdPartyAuthenticationStep(
            final ConsentManager consentManager,
            final SibsAuthenticator sibsAuthenticator,
            final StrongAuthenticationState strongAuthenticationState) {
        this.strongAuthenticationState = strongAuthenticationState;
        consentStatusRetryer = createConsentStatusRetryer();
        this.consentManager = consentManager;
        this.authenticator = sibsAuthenticator;
    }

    private Retryer<ConsentStatus> createConsentStatusRetryer() {
        return RetryerBuilder.<ConsentStatus>newBuilder()
                .retryIfResult(status -> status != null && !status.isFinalStatus())
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }

    static ThirdPartyAppAuthenticationStep create(
            final ConsentManager consentManager,
            SibsAuthenticator sibsAuthenticator,
            StrongAuthenticationState strongAuthenticationState) {
        SibsThirdPartyAuthenticationStep factory =
                new SibsThirdPartyAuthenticationStep(
                        consentManager, sibsAuthenticator, strongAuthenticationState);
        return new ThirdPartyAppAuthenticationStep(
                factory.getAppPayload(),
                factory.getWaitRequest(),
                factory::processThirdPartyCallback);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload() {
        return ThirdPartyAppAuthenticationPayload.of(consentManager.create());
    }

    private SupplementalWaitRequest getWaitRequest() {
        return new SupplementalWaitRequest(
                strongAuthenticationState.getSupplementalKey(), SLEEP_TIME, TimeUnit.MINUTES);
    }

    private void processThirdPartyCallback(Map<String, String> callbackData)
            throws AuthorizationException {
        boolean authFailed = false;
        try {
            ConsentStatus consentStatus = consentStatusRetryer.call(consentManager::getStatus);
            if (consentStatus.isAcceptedStatus()) {
                authenticator.handleManualAuthenticationSuccess();
                return;
            } else {
                authFailed = true;
            }
        } catch (ExecutionException | RetryException e) {
            logger.warn("Authorization failed, consents status is not accepted.", e);
            authFailed = true;
        }
        if (authFailed) {
            authenticator.handleManualAuthenticationFailure();
            throw new AuthorizationException(
                    AuthorizationError.UNAUTHORIZED,
                    "Authorization failed, consents status is not accepted.");
        }
    }
}
