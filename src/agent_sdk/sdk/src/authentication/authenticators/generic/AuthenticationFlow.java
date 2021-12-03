package se.tink.agent.sdk.authentication.authenticators.generic;

import com.google.common.collect.ImmutableList;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;

public class AuthenticationFlow<T> {
    private final Class<? extends T> startStep;
    private final ImmutableList<T> steps;

    AuthenticationFlow(Class<? extends T> startStep, ImmutableList<T> steps) {
        this.startStep = startStep;
        this.steps = steps;
    }

    public Class<? extends T> getStartStep() {
        return startStep;
    }

    public ImmutableList<T> getSteps() {
        return steps;
    }

    public static AuthenticationFlowBuilder<NewConsentStep> builder(NewConsentStep entryPoint) {
        return new AuthenticationFlowBuilder<>(entryPoint);
    }

    public static AuthenticationFlowBuilder<ExistingConsentStep> builder(
            ExistingConsentStep entryPoint) {
        return new AuthenticationFlowBuilder<>(entryPoint);
    }
}
