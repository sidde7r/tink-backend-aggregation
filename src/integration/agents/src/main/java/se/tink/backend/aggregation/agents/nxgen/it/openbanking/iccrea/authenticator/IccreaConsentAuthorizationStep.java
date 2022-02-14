package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;

@RequiredArgsConstructor
@Slf4j
public class IccreaConsentAuthorizationStep {

    private static final int DEFAULT_SLEEP_TIME = 3;
    private static final int DEFAULT_RETRY_ATTEMPTS = 60;

    private final CbiGlobeAuthApiClient authApiClient;
    private final UserInteractions userInteractions;
    private final CbiStorage storage;

    private final long sleepTimeSeconds;
    private final int retryAttempts;

    public IccreaConsentAuthorizationStep(
            CbiGlobeAuthApiClient authApiClient,
            UserInteractions userInteractions,
            CbiStorage storage) {
        this(authApiClient, userInteractions, storage, DEFAULT_SLEEP_TIME, DEFAULT_RETRY_ATTEMPTS);
    }

    public void authorizeConsent() {
        userInteractions.displayPromptAndWaitForAcceptance();
        pollForConsentStatusAndThrowIfNotValid();
    }

    private void pollForConsentStatusAndThrowIfNotValid() {
        CbiConsentStatusResponse consentStatusResponse = retryCallForConsentStatus();

        if (!consentStatusResponse.getConsentStatus().isValid()) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(
                    "Consent status after decoupled polling was final, but not valid: "
                            + consentStatusResponse.getConsentStatus());
        }
    }

    @SneakyThrows
    private CbiConsentStatusResponse retryCallForConsentStatus() {
        Retryer<CbiConsentStatusResponse> approvalStatusRetryer = getApprovalStatusRetryer();
        try {
            return approvalStatusRetryer.call(
                    () -> authApiClient.fetchConsentStatus(storage.getConsentId()));
        } catch (RetryException e) {
            throw ThirdPartyAppError.TIMED_OUT.exception(
                    "Consent status not in final state after "
                            + DEFAULT_RETRY_ATTEMPTS
                            + " checks.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (!(cause instanceof AgentException)) {
                log.error("Unhandled error while checking decoupled auth!", e);
            }
            throw cause;
        }
    }

    private Retryer<CbiConsentStatusResponse> getApprovalStatusRetryer() {
        return RetryerBuilder.<CbiConsentStatusResponse>newBuilder()
                .retryIfResult(
                        consentStatusResponse ->
                                consentStatusResponse.getConsentStatus() == null
                                        || !consentStatusResponse.getConsentStatus().isFinal())
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTimeSeconds, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryAttempts))
                .build();
    }
}
