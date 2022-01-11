package se.tink.agent.sdk.authentication.common_steps;

import java.util.Optional;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;
import se.tink.agent.sdk.user_interaction.SupplementalInformation;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.user_interaction.UserResponseData;

public abstract class SupplementalInformationStep extends InteractiveStep<ConsentLifetime> {

    @Override
    public InteractiveStepResponse<ConsentLifetime> execute(StepRequest request) {
        Optional<UserResponseData> optionalUserResponseData = request.getUserResponseData();

        if (optionalUserResponseData.isPresent()) {
            return this.handleUserResponse(optionalUserResponseData.get());
        } else {
            SupplementalInformation supplementalInformation = this.getSupplementalInformation();

            UserInteraction<SupplementalInformation> userInteraction =
                    UserInteraction.supplementalInformation(supplementalInformation)
                            .userResponseRequired()
                            .build();

            // Visit ourselves again when we get a userInteraction response.
            return InteractiveStepResponse.nextStep(this.getClass())
                    .userInteraction(userInteraction)
                    .build();
        }
    }

    public abstract SupplementalInformation getSupplementalInformation();

    public abstract InteractiveStepResponse<ConsentLifetime> handleUserResponse(
            UserResponseData response);
}
