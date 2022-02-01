package se.tink.agent.sdk.steppable_execution.execution_flow;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;

public interface ExecutionFlow<T, R> {
    /**
     * @param stepId The step identifier
     * @return Optional.empty() if the identifier could not be found among the registered steps,
     *     otherwise Optional.of(step)
     */
    Optional<BaseStep<T, R>> getStep(@Nullable String stepId);
}
