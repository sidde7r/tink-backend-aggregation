package se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps;

import java.time.Duration;
import java.util.Optional;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppPollStatus;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppResult;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.storage.SerializableReference;
import se.tink.agent.sdk.storage.Storage;
import se.tink.agent.sdk.utils.Sleeper;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;

public class ThirdPartyAppPollStep implements NewConsentStep {
    private static final Integer DEFAULT_MAX_POLL_ATTEMPTS = 90;
    private static final Duration DEFAULT_WAIT_DURATION = Duration.ofSeconds(1);

    private final Integer maxPollAttempts;
    private final ThirdPartyAppPollStatus agentPollStatus;
    private final Sleeper sleeper;

    private final Class<? extends NewConsentStep> reopenAppStep;
    private final Class<? extends NewConsentStep> nextStep;

    public ThirdPartyAppPollStep(
            Integer maxPollAttempts,
            ThirdPartyAppPollStatus agentPollStatus,
            Sleeper sleeper,
            Class<? extends NewConsentStep> reopenAppStep,
            Class<? extends NewConsentStep> nextStep) {
        this.maxPollAttempts = maxPollAttempts;
        this.agentPollStatus = agentPollStatus;
        this.sleeper = sleeper;
        this.reopenAppStep = reopenAppStep;
        this.nextStep = nextStep;
    }

    public ThirdPartyAppPollStep(
            ThirdPartyAppPollStatus agentPollStatus,
            Sleeper sleeper,
            Class<? extends NewConsentStep> reopenAppStep,
            Class<? extends NewConsentStep> nextStep) {
        this(DEFAULT_MAX_POLL_ATTEMPTS, agentPollStatus, sleeper, reopenAppStep, nextStep);
    }

    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
        Storage authenticationStorage = request.getAuthenticationStorage();

        checkAndIncrementAttemptsCounter(authenticationStorage);

        // Sleep a short while between attempts.
        this.sleeper.sleep(DEFAULT_WAIT_DURATION);

        SerializableReference reference =
                authenticationStorage
                        .tryGet(
                                ThirdPartyAppAuthenticator.STATE_KEY_REFERENCE,
                                SerializableReference.class)
                        .orElse(null);

        ThirdPartyAppResult thirdPartyAppResult =
                this.agentPollStatus.pollThirdPartyAppStatus(reference);

        SerializableReference newReference = thirdPartyAppResult.getReference();

        // Rewrite the reference to the state for next iteration. Fall back to previous reference if
        // a new one was not given.
        authenticationStorage.put(
                ThirdPartyAppAuthenticator.STATE_KEY_REFERENCE,
                Optional.ofNullable(newReference).orElse(reference));

        return handlePollResult(thirdPartyAppResult);
    }

    private void checkAndIncrementAttemptsCounter(Storage authenticationStorage) {
        Integer counter =
                authenticationStorage
                        .tryGet(ThirdPartyAppAuthenticator.STATE_KEY_COUNTER, Integer.class)
                        .orElse(0);
        if (counter > this.maxPollAttempts) {
            // Timeout
            throw ThirdPartyAppError.TIMED_OUT.exception();
        }
        authenticationStorage.put(ThirdPartyAppAuthenticator.STATE_KEY_COUNTER, counter + 1);
    }

    private NewConsentResponse handlePollResult(ThirdPartyAppResult pollResult) {
        switch (pollResult.getStatus()) {
            case DONE:
                return NewConsentResponse.nextStep(this.nextStep).noUserInteraction().build();
            case REINITIATE_APP:
                return NewConsentResponse.nextStep(this.reopenAppStep).noUserInteraction().build();
            case PENDING:
                return NewConsentResponse.nextStep(this.getClass()).noUserInteraction().build();
            case NO_CLIENT:
                throw new IllegalStateException("NO_CLIENT");
            case ALREADY_IN_PROGRESS:
                throw ThirdPartyAppError.ALREADY_IN_PROGRESS.exception();
            case CANCELLED:
                throw new IllegalStateException("CANCELLED");
            case TIMED_OUT:
                throw ThirdPartyAppError.TIMED_OUT.exception();
            case UNKNOWN_FAILURE:
                throw new IllegalStateException("UNKNOWN_FAILURE");
            default:
                throw new IllegalStateException("Unexpected value: " + pollResult.getStatus());
        }
    }
}
