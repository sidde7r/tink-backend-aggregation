package se.tink.agent.sdk.authentication.authenticators.generic;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;

public class AuthenticationFlowBuilder<T> {
    private final String startStepId;
    private final HashMap<String, T> steps;

    AuthenticationFlowBuilder(T entryPoint) {
        this.startStepId = entryPoint.getClass().toString();
        this.steps = new HashMap<>();
        addStep(entryPoint);
    }

    public AuthenticationFlowBuilder<T> addStep(T step) {
        String stepId = step.getClass().toString();
        if (steps.containsKey(stepId)) {
            throw new IllegalStateException(
                    String.format("Duplicate authentication step added: '%s'.", stepId));
        }
        this.steps.put(stepId, step);
        return this;
    }

    public AuthenticationFlow<T> build() {
        ImmutableMap<String, T> immutableSteps = ImmutableMap.copyOf(this.steps);
        return new AuthenticationFlow<>(this.startStepId, immutableSteps);
    }
}
