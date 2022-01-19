package se.tink.agent.sdk.steppable_execution.execution_flow;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;

public interface ExecutionFlow<T, R> {
    Optional<BaseStep<T, R>> getStep(@Nullable String stepId);
}
