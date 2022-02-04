package src.agent_sdk.compatibility_layers.aggregation_service.src.steppable_execution;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.agent.runtime.steppable_execution.SteppableExecutor;
import se.tink.agent.runtime.user_interaction.UserResponseDataImpl;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlow;
import se.tink.agent.sdk.storage.SerializableStorage;
import se.tink.agent.sdk.storage.Storage;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.user_interaction.UserResponseData;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public class AggregationServiceSteppableExecutor {
    private static final Duration SUPPLEMENTAL_INFORMATION_WAIT_TIME = Duration.ofMinutes(9);

    private final SupplementalInformationController supplementalInformationController;
    private final Storage agentStorage;

    public AggregationServiceSteppableExecutor(
            SupplementalInformationController supplementalInformationController,
            Storage agentStorage) {
        this.supplementalInformationController = supplementalInformationController;
        this.agentStorage = agentStorage;
    }

    /**
     * Fully and synchronous execute an execution flow till it returns. This method will signal the
     * user for user interaction (e.g. to open a third party app or respond with supplemental
     * information), and if needed also wait for a user response, if an execution step requires.
     *
     * <p>It will only return when the whole execution flow has completed, i.e. one step has
     * returned a response with a donePayload.
     *
     * @param executionFlow The execution flow to execute.
     * @param stepArgument The argument which will be given to every step execution.
     * @param <T> Type of step argument.
     * @param <R> Type of step return value.
     * @return The return value, of type R, of the execution flow.
     */
    public <T, R> R execute(ExecutionFlow<T, R> executionFlow, T stepArgument) {
        // Initial values.
        SteppableExecutor<T, R> executor = new SteppableExecutor<>(executionFlow);
        SerializableStorage stepStorage = new SerializableStorage();
        String stepId = null;
        UserResponseData userResponseData = null;

        while (true) {
            StepRequest<T> request =
                    new StepRequest<>(
                            stepArgument, stepStorage, this.agentStorage, userResponseData);

            StepResponse<R> response = executor.execute(stepId, request);
            if (response.getDonePayload().isPresent()) {
                return response.getDonePayload().get();
            }

            userResponseData = handleUserInteraction(response).orElse(null);

            stepId =
                    response.getNextStepId()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Step response did not contain a nextStepId."));
        }
    }

    private Optional<UserResponseData> handleUserInteraction(StepResponse<?> response) {
        if (!response.getUserInteraction().isPresent()) {
            return Optional.empty();
        }

        UserInteraction<?> userInteraction = response.getUserInteraction().get();

        String mfaId =
                supplementalInformationController.requestUserInteractionAsync(userInteraction);

        if (!userInteraction.isUserResponseRequired()) {
            // No user response is required/expected, we can therefore return immediately.
            return Optional.empty();
        }

        Map<String, String> userResponse =
                supplementalInformationController
                        .waitForSupplementalInformation(
                                mfaId,
                                SUPPLEMENTAL_INFORMATION_WAIT_TIME.toMinutes(),
                                TimeUnit.MINUTES)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "Supplemental information timed out for mfaId: '%s'.",
                                                        mfaId)));

        UserResponseData userResponseData = new UserResponseDataImpl(userResponse);
        return Optional.of(userResponseData);
    }
}
