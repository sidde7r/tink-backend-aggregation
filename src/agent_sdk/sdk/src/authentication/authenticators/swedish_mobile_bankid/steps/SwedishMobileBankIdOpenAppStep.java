package se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.steps;

import se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.SwedishMobileBankIdGetAutostartToken;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.IntermediateStepResponse;
import se.tink.agent.sdk.storage.SerializableReference;
import se.tink.agent.sdk.user_interaction.SwedishMobileBankIdInfo;

public class SwedishMobileBankIdOpenAppStep extends IntermediateStep {

    private final SwedishMobileBankIdGetAutostartToken agentGetAutostartToken;
    private final Class<? extends BaseStep<?, ?>> nextStep;

    public SwedishMobileBankIdOpenAppStep(
            SwedishMobileBankIdGetAutostartToken agentGetAutostartToken,
            Class<? extends BaseStep<?, ?>> nextStep) {
        this.agentGetAutostartToken = agentGetAutostartToken;
        this.nextStep = nextStep;
    }

    @Override
    public IntermediateStepResponse execute(StepRequest<Void> request) {
        SerializableReference reference =
                request.getStepStorage()
                        .tryGet(
                                ThirdPartyAppAuthenticator.STATE_KEY_REFERENCE,
                                SerializableReference.class)
                        .orElse(null);

        SwedishMobileBankIdInfo swedishMobileBankIdInfo =
                this.agentGetAutostartToken.getAutostartToken(reference);

        return IntermediateStepResponse.nextStep(this.nextStep)
                .userInteraction(swedishMobileBankIdInfo.intoUserInteraction())
                .build();
    }
}
