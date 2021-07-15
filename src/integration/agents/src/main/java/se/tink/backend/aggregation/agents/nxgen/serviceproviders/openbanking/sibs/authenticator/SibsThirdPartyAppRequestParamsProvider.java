package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class SibsThirdPartyAppRequestParamsProvider implements ThirdPartyAppRequestParamsProvider {

    private static final long SLEEP_TIME = 10L;
    static final String STEP_ID = "sibsThirdPartyAuthenticationStep";
    private static final int WAIT_IN_SECONDS_BETWEEN_REQUESTS = 5;
    private static final long TIMEOUT_SECONDS = 30;

    private final StrongAuthenticationState strongAuthenticationState;
    private final ConsentManager consentManager;
    private final SibsAuthenticator authenticator;
    private final LocalDateTimeSource localDateTimeSource;
    private final SibsRetryTimeConfiguration sibsRetryTimeConfiguration;

    SibsThirdPartyAppRequestParamsProvider(
            final ConsentManager consentManager,
            final SibsAuthenticator sibsAuthenticator,
            final StrongAuthenticationState strongAuthenticationState,
            final LocalDateTimeSource localDateTimeSource) {
        this(
                consentManager,
                sibsAuthenticator,
                strongAuthenticationState,
                localDateTimeSource,
                new SibsRetryTimeConfiguration(WAIT_IN_SECONDS_BETWEEN_REQUESTS, TIMEOUT_SECONDS));
    }

    SibsThirdPartyAppRequestParamsProvider(
            final ConsentManager consentManager,
            final SibsAuthenticator sibsAuthenticator,
            final StrongAuthenticationState strongAuthenticationState,
            final LocalDateTimeSource localDateTimeSource,
            final SibsRetryTimeConfiguration sibsRetryTimeConfiguration) {
        this.strongAuthenticationState = strongAuthenticationState;
        this.consentManager = consentManager;
        this.authenticator = sibsAuthenticator;
        this.localDateTimeSource = localDateTimeSource;
        this.sibsRetryTimeConfiguration = sibsRetryTimeConfiguration;
    }

    AuthenticationStepResponse processThirdPartyCallback(Map<String, String> callbackData)
            throws AuthorizationException {

        LocalDateTime timeoutThreshold =
                localDateTimeSource.now().plusSeconds(sibsRetryTimeConfiguration.getTimeout());
        while (isTimePassed(timeoutThreshold)) {
            ConsentStatus consentStatus = executeWithDelay(consentManager::getStatus);
            if (consentStatus.isAcceptedStatus()) {
                authenticator.handleManualAuthenticationSuccess();
                return AuthenticationStepResponse.executeNextStep();
            }
        }
        authenticator.handleManualAuthenticationFailure();
        throw new AuthorizationException(
                AuthorizationError.UNAUTHORIZED,
                "Authorization failed, consents status is not accepted.");
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getPayload() {
        return ThirdPartyAppAuthenticationPayload.of(consentManager.create());
    }

    @Override
    public SupplementalWaitRequest getWaitingConfiguration() {
        return new SupplementalWaitRequest(
                strongAuthenticationState.getSupplementalKey(), SLEEP_TIME, TimeUnit.MINUTES);
    }

    private boolean isTimePassed(LocalDateTime timeout) {
        return localDateTimeSource.now().isBefore(timeout);
    }

    private <T> T executeWithDelay(CheckedFunction0<T> function) {
        return Try.of(function)
                .andThenTry(() -> Thread.sleep(sibsRetryTimeConfiguration.getWaitingTime() * 1000L))
                .get();
    }
}
