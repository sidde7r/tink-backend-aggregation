package se.tink.agent.sdk.steppable_execution.execution_flow;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;

public class ExecutionFlowImpl<T, R> implements ExecutionFlow<T, R> {
    private final String startStepId;
    private final ImmutableMap<String, BaseStep<T, R>> steps;

    ExecutionFlowImpl(String startStepId, ImmutableMap<String, BaseStep<T, R>> steps) {
        this.startStepId = startStepId;
        this.steps = steps;
    }

    @Override
    public Optional<BaseStep<T, R>> getStep(@Nullable String stepId) {
        // Pick the startStepId if `stepId` is null.
        String stepIdToFind = Optional.ofNullable(stepId).orElse(this.startStepId);
        return Optional.ofNullable(this.steps.get(stepIdToFind));
    }
}
