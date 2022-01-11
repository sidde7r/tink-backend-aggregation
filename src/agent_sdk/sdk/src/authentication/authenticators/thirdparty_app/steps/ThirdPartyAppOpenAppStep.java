package se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps;

import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppGetAppInfo;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.IntermediateStepResponse;
import se.tink.agent.sdk.storage.SerializableReference;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public class ThirdPartyAppOpenAppStep extends IntermediateStep {

    private final ThirdPartyAppGetAppInfo agentGetAppInfo;
    private final Class<? extends BaseStep<?>> nextStep;

    public ThirdPartyAppOpenAppStep(
            ThirdPartyAppGetAppInfo agentGetAppInfo, Class<? extends BaseStep<?>> nextStep) {
        this.agentGetAppInfo = agentGetAppInfo;
        this.nextStep = nextStep;
    }

    @Override
    public IntermediateStepResponse execute(StepRequest request) {
        SerializableReference reference =
                request.getStepStorage()
                        .tryGet(
                                ThirdPartyAppAuthenticator.STATE_KEY_REFERENCE,
                                SerializableReference.class)
                        .orElse(null);

        UserInteraction<ThirdPartyAppInfo> thirdPartyAppInfo =
                this.agentGetAppInfo.getThirdPartyAppInfo(reference);

        return IntermediateStepResponse.nextStep(this.nextStep)
                .userInteraction(thirdPartyAppInfo)
                .build();
    }
}
