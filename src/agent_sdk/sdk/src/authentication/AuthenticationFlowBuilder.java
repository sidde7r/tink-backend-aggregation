package se.tink.agent.sdk.authentication;

import com.google.common.collect.ImmutableList;

public class AuthenticationFlowBuilder<T> {
    private final Class<? extends T> startStep;
    private final ImmutableList.Builder<T> steps;

    AuthenticationFlowBuilder(T entryPoint) {
        this.startStep = (Class<? extends T>) entryPoint.getClass();
        this.steps = ImmutableList.<T>builder().add(entryPoint);
    }

    public AuthenticationFlowBuilder<T> addStep(T step) {
        this.steps.add(step);
        return this;
    }

    public AuthenticationFlow<T> build() {
        return new AuthenticationFlow<>(this.startStep, this.steps.build());
    }
}
