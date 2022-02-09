package se.tink.agent.runtime.steppable_execution;

import com.google.common.base.Preconditions;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nullable;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlow;

public class SteppableExecutor<T, R> {
    private static final Duration DEFAULT_MAX_EXECUTION_TIME = Duration.ofSeconds(20);

    private final Duration maxExecutionTime;
    private final ExecutionFlow<T, R> executionFlow;

    public SteppableExecutor(ExecutionFlow<T, R> executionFlow) {
        this(DEFAULT_MAX_EXECUTION_TIME, executionFlow);
    }

    public SteppableExecutor(Duration maxExecutionTime, ExecutionFlow<T, R> executionFlow) {
        this.maxExecutionTime = Preconditions.checkNotNull(maxExecutionTime);
        this.executionFlow = Preconditions.checkNotNull(executionFlow);
    }

    /**
     * Execute one or more steps in the execution flow. This method will always return a response if
     * the response indicates user interaction is needed or if it's done. It will also return if
     * execution time exceeds the maximum allowed execution time.
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
        Instant startTime = Instant.now();

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

            // Break the execution loop if the maximum execution time has been exceeded.
            // This is to mitigate long-running agent processes.
            if (hasExceededExecutionTime(startTime)) {
                return response;
            }
        }
    }

    private boolean hasExceededExecutionTime(Instant startTime) {
        Instant currentTime = Instant.now();
        Duration timeSpent = Duration.between(startTime, currentTime);
        return timeSpent.compareTo(this.maxExecutionTime) >= 0;
    }

    public static class StepNotFoundException extends RuntimeException {}
}
