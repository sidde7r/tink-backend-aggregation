package src.agent_sdk.runtime.src.steppable_execution;

import javax.annotation.Nullable;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlow;

public class SteppableExecutor<T, R> {
    private final ExecutionFlow<T, R> executionFlow;

    public SteppableExecutor(ExecutionFlow<T, R> executionFlow) {
        this.executionFlow = executionFlow;
    }

    /**
     * Execute one or more steps in the execution flow. This method will always return a response if
     * the response indicates user interaction is needed or if it's done.
     *
     * @param stepId The step identifier to start executing. Null, equals no prior state, means that
     *     it should start on the start step.
     * @param request The step request given to the steps to execute.
     * @return A step response, the response itself can indicate if user interaction is needed or if
     *     the execution is done. However, you should never assume that the response will always be
     *     either UserInteraction or Done.
     * @throws StepNotFoundException This exception is thrown if the stepId cannot be found or if a
     *     step returns a nextStepId that cannot be found in the execution flow.
     */
    public StepResponse<R> execute(@Nullable String stepId, StepRequest<T> request)
            throws StepNotFoundException {
        while (true) {
            BaseStep<T, R> stepToExecute =
                    this.executionFlow.getStep(stepId).orElseThrow(StepNotFoundException::new);

            StepResponse<R> response = stepToExecute.executeInternal(request);
            if (response.getDonePayload().isPresent()
                    || response.getUserInteraction().isPresent()) {
                return response;
            }

            stepId =
                    response.getNextStepId()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Step response did not contain a nextStepId."));
        }
    }

    public static class StepNotFoundException extends RuntimeException {}
}
