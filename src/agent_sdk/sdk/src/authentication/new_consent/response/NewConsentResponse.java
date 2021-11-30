package se.tink.agent.sdk.authentication.new_consent.response;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.ToString;
import se.tink.agent.sdk.authentication.new_consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.builder.UserInteractionBuildStep;
import se.tink.agent.sdk.user_interaction.UserInteraction;

@ToString
@Getter
public class NewConsentResponse {
    private final boolean completed;
    @Nullable private final ConsentLifetime consentLifetime;
    @Nullable private final Class<? extends NewConsentStep> nextStep;
    @Nullable private final UserInteraction<?> userInteraction;

    NewConsentResponse(
            boolean completed,
            @Nullable ConsentLifetime consentLifetime,
            @Nullable Class<? extends NewConsentStep> nextStep,
            @Nullable UserInteraction<?> userInteraction) {
        this.completed = completed;
        this.consentLifetime = consentLifetime;
        this.nextStep = nextStep;
        this.userInteraction = userInteraction;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Optional<ConsentLifetime> getConsentLifetime() {
        return Optional.ofNullable(consentLifetime);
    }

    public Optional<Class<? extends NewConsentStep>> getNextStep() {
        return Optional.ofNullable(nextStep);
    }

    public Optional<UserInteraction<?>> getUserInteraction() {
        return Optional.ofNullable(userInteraction);
    }

    public static UserInteractionBuildStep nextStep(Class<? extends NewConsentStep> nextStep) {
        Preconditions.checkNotNull(nextStep, "nextStep cannot be null!");
        return new NewConsentResponseNextBuilder(nextStep);
    }

    public static NewConsentResponse done(ConsentLifetime consentLifetime) {
        Preconditions.checkNotNull(consentLifetime, "consentLifetime cannot be null!");
        return new NewConsentResponse(true, consentLifetime, null, null);
    }
}
