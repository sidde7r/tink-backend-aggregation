package se.tink.agent.sdk.authentication.authenticators.generic;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;

public class AuthenticationFlow<T> {
    private final String startStepId;
    private final ImmutableMap<String, T> steps;

    AuthenticationFlow(String startStepId, ImmutableMap<String, T> steps) {
        this.startStepId = startStepId;
        this.steps = steps;
    }

    public Optional<T> getStep(@Nullable String stepId) {
        // Pick the start step if `stepId` is null.
        String stepIdToFind = Optional.ofNullable(stepId).orElse(this.startStepId);

        return Optional.ofNullable(this.steps.get(stepIdToFind));
    }

    public static AuthenticationFlowBuilder<NewConsentStep> builder(NewConsentStep entryPoint) {
        return new AuthenticationFlowBuilder<>(entryPoint);
    }

    public static AuthenticationFlowBuilder<ExistingConsentStep> builder(
            ExistingConsentStep entryPoint) {
        return new AuthenticationFlowBuilder<>(entryPoint);
    }
}
