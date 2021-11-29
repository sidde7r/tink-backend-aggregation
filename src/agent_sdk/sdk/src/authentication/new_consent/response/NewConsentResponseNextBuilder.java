package se.tink.agent.sdk.authentication.new_consent.response;

import javax.annotation.Nullable;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.builder.BuildStep;
import se.tink.agent.sdk.authentication.new_consent.response.builder.UserInteractionBuildStep;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public class NewConsentResponseNextBuilder implements UserInteractionBuildStep, BuildStep {
    private final Class<? extends NewConsentStep> nextStep;

    @Nullable private UserInteraction<?> userInteraction;

    NewConsentResponseNextBuilder(Class<? extends NewConsentStep> nextStep) {
        this.nextStep = nextStep;
    }

    @Override
    public BuildStep userInteraction(UserInteraction<?> userInteraction) {
        this.userInteraction = userInteraction;
        return this;
    }

    @Override
    public BuildStep noUserInteraction() {
        this.userInteraction = null;
        return this;
    }

    @Override
    public NewConsentResponse build() {
        return new NewConsentResponse(false, null, this.nextStep, this.userInteraction);
    }
}
