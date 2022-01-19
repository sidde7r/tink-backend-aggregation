package se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps;

import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppInitAuthentication;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result.ThirdPartyAppResult;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result.ThirdPartyAppStatus;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.IntermediateStepResponse;
import se.tink.agent.sdk.storage.SerializableReference;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;

public class ThirdPartyAppInitStep extends IntermediateStep {
    private final ThirdPartyAppInitAuthentication agentInitAuthentication;
    private final Class<? extends BaseStep<?, ?>> nextStep;

    public ThirdPartyAppInitStep(
            ThirdPartyAppInitAuthentication agentInitAuthentication,
            Class<? extends BaseStep<?, ?>> nextStep) {
        this.agentInitAuthentication = agentInitAuthentication;
        this.nextStep = nextStep;
    }

    @Override
    public IntermediateStepResponse execute(StepRequest<Void> request) {
        ThirdPartyAppResult thirdPartyAppResult =
                this.agentInitAuthentication.initThirdPartyAppAuthentication();
        handleStatus(thirdPartyAppResult.getStatus());

        SerializableReference reference = thirdPartyAppResult.getReference().orElse(null);
        request.getStepStorage().put(ThirdPartyAppAuthenticator.STATE_KEY_REFERENCE, reference);

        return IntermediateStepResponse.nextStep(this.nextStep).noUserInteraction().build();
    }

    private void handleStatus(ThirdPartyAppStatus status) {
        // TODO: use proper exceptions.
        switch (status) {
            case PENDING:
                break;

            case NO_CLIENT:
                throw new IllegalStateException("NO_CLIENT");

            case ALREADY_IN_PROGRESS:
                throw ThirdPartyAppError.ALREADY_IN_PROGRESS.exception();

            case UNKNOWN_FAILURE:
                throw new IllegalStateException("UNKNOWN_FAILURE");

            default:
                throw new IllegalStateException(
                        String.format("Unexpected and invalid status: %s.", status));
        }
    }
}
