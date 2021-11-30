package se.tink.agent.sdk.authentication.common_steps;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.user_interaction.UserResponseData;

public abstract class SupplementalInformationStep implements NewConsentStep {
    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
        Optional<UserResponseData> optionalUserResponseData = request.getUserResponseData();

        if (optionalUserResponseData.isPresent()) {
            return this.handleUserResponse(optionalUserResponseData.get());
        } else {
            ImmutableList<String> fields = this.getFields();

            UserInteraction<ImmutableList<String>> userInteraction =
                    UserInteraction.supplementalInformation(fields).userResponseRequired().build();

            // Visit ourselves again when we get a userInteraction response.
            return NewConsentResponse.nextStep(this.getClass())
                    .userInteraction(userInteraction)
                    .build();
        }
    }

    // TODO: Update fields type.
    public abstract ImmutableList<String> getFields();

    public abstract NewConsentResponse handleUserResponse(UserResponseData response);
}
