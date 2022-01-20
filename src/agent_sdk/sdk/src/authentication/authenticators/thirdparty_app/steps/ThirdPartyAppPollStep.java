package se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps;

import java.time.Duration;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppPollStatus;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result.ThirdPartyAppResult;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.IntermediateStepResponse;
import se.tink.agent.sdk.storage.SerializableReference;
import se.tink.agent.sdk.storage.Storage;
import se.tink.agent.sdk.utils.Sleeper;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;

public class ThirdPartyAppPollStep extends IntermediateStep {
    private static final Integer DEFAULT_MAX_POLL_ATTEMPTS = 90;
    private static final Duration DEFAULT_WAIT_DURATION = Duration.ofSeconds(1);

    private final Integer maxPollAttempts;
    private final ThirdPartyAppPollStatus agentPollStatus;
    private final Sleeper sleeper;

    private final Class<? extends BaseStep<?, ?>> reopenAppStep;
    private final Class<? extends BaseStep<?, ?>> nextStep;

    public ThirdPartyAppPollStep(
            Integer maxPollAttempts,
            ThirdPartyAppPollStatus agentPollStatus,
            Sleeper sleeper,
            Class<? extends BaseStep<?, ?>> reopenAppStep,
            Class<? extends BaseStep<?, ?>> nextStep) {
        this.maxPollAttempts = maxPollAttempts;
        this.agentPollStatus = agentPollStatus;
        this.sleeper = sleeper;
        this.reopenAppStep = reopenAppStep;
        this.nextStep = nextStep;
    }

    public ThirdPartyAppPollStep(
            ThirdPartyAppPollStatus agentPollStatus,
            Sleeper sleeper,
            Class<? extends BaseStep<?, ?>> reopenAppStep,
            Class<? extends BaseStep<?, ?>> nextStep) {
        this(DEFAULT_MAX_POLL_ATTEMPTS, agentPollStatus, sleeper, reopenAppStep, nextStep);
    }

    @Override
    public IntermediateStepResponse execute(StepRequest<Void> request) {
        Storage stepStorage = request.getStepStorage();

        checkAndIncrementAttemptsCounter(stepStorage);

        // Sleep a short while between attempts.
        this.sleeper.sleep(DEFAULT_WAIT_DURATION);

        SerializableReference reference =
                stepStorage
                        .tryGet(
                                ThirdPartyAppAuthenticator.STATE_KEY_REFERENCE,
                                SerializableReference.class)
                        .orElse(null);

        ThirdPartyAppResult thirdPartyAppResult =
                this.agentPollStatus.pollThirdPartyAppStatus(reference);

        // Rewrite the reference to the state for next iteration. Fall back to previous reference if
        // a new one was not given.
        SerializableReference newReference = thirdPartyAppResult.getReference().orElse(reference);
        stepStorage.put(ThirdPartyAppAuthenticator.STATE_KEY_REFERENCE, newReference);

        return handlePollResult(thirdPartyAppResult);
    }

    private void checkAndIncrementAttemptsCounter(Storage stepStorage) {
        Integer counter =
                stepStorage
                        .tryGet(ThirdPartyAppAuthenticator.STATE_KEY_COUNTER, Integer.class)
                        .orElse(0);
        if (counter > this.maxPollAttempts) {
            // Timeout
            throw ThirdPartyAppError.TIMED_OUT.exception();
        }
        stepStorage.put(ThirdPartyAppAuthenticator.STATE_KEY_COUNTER, counter + 1);
    }

    private IntermediateStepResponse handlePollResult(ThirdPartyAppResult pollResult) {
        switch (pollResult.getStatus()) {
            case DONE:
                return IntermediateStepResponse.nextStep(this.nextStep).noUserInteraction().build();
            case REINITIATE_APP:
                return IntermediateStepResponse.nextStep(this.reopenAppStep)
                        .noUserInteraction()
                        .build();
            case PENDING:
                return IntermediateStepResponse.nextStep(this.getClass())
                        .noUserInteraction()
                        .build();
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
