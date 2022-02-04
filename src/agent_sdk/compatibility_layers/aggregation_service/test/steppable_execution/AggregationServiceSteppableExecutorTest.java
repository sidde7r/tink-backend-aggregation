package src.agent_sdk.compatibility_layers.aggregation_service.test.steppable_execution;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.agent.runtime.steppable_execution.SteppableExecutor;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowBuilder;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowImpl;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.InteractiveFlowStartStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;
import se.tink.agent.sdk.storage.SerializableStorage;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.user_interaction.UserResponseData;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import src.agent_sdk.compatibility_layers.aggregation_service.src.steppable_execution.AggregationServiceSteppableExecutor;

public class AggregationServiceSteppableExecutorTest {
    private AggregationServiceSteppableExecutor executor;

    @Before
    public void init() {
        Map<String, String> rawUserResponseData = new HashMap<>();
        rawUserResponseData.put("foo", "bar");

        SupplementalInformationController supplementalInformationController =
                Mockito.mock(SupplementalInformationController.class);

        Mockito.when(supplementalInformationController.requestUserInteractionAsync(Mockito.any()))
                .thenReturn("foobar");

        Mockito.when(
                        supplementalInformationController.waitForSupplementalInformation(
                                Mockito.eq("foobar"), Mockito.anyLong(), Mockito.any()))
                .thenReturn(Optional.of(rawUserResponseData));

        SerializableStorage agentStorage = new SerializableStorage();
        this.executor =
                new AggregationServiceSteppableExecutor(
                        supplementalInformationController, agentStorage);
    }

    @Test(expected = SteppableExecutor.StepNotFoundException.class)
    public void testStepNotFound() {
        StepFlow stepFlow = StepFlow.builder().startStep(new StepA()).build();

        this.executor.execute(stepFlow, "foo");
    }

    @Test
    public void testFullFlow() {
        StepFlow stepFlow =
                StepFlow.builder()
                        .startStep(new StepA())
                        .addStep(new StepB())
                        .addStep(new StepC())
                        .build();

        Integer returnValue = this.executor.execute(stepFlow, "foo");
        Assert.assertEquals(Integer.valueOf(123), returnValue);
    }

    private static class StepA extends InteractiveStep<String, Integer> {
        @Override
        public InteractiveStepResponse<Integer> execute(StepRequest<String> request) {
            return InteractiveStepResponse.nextStep(StepB.class).noUserInteraction().build();
        }
    }

    private static class StepB extends InteractiveStep<String, Integer> {
        @Override
        public InteractiveStepResponse<Integer> execute(StepRequest<String> request) {
            return InteractiveStepResponse.nextStep(StepC.class)
                    .userInteraction(
                            UserInteraction.thirdPartyApp(
                                            ThirdPartyAppInfo.of(URL.of("http://example.com")))
                                    .userResponseRequired("foobar")
                                    .build())
                    .build();
        }
    }

    private static class StepC extends InteractiveStep<String, Integer> {
        @Override
        public InteractiveStepResponse<Integer> execute(StepRequest<String> request) {
            UserResponseData userResponseData =
                    request.getUserResponseData()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Expected user response data."));
            Assert.assertEquals(Optional.of("bar"), userResponseData.tryGet("foo"));
            return InteractiveStepResponse.done(123);
        }
    }

    private static class StepFlow extends ExecutionFlowImpl<String, Integer> {
        private StepFlow(String startStepId, Map<String, BaseStep<String, Integer>> steps) {
            super(startStepId, steps);
        }

        public static InteractiveFlowStartStep<String, Integer, StepFlow> builder() {
            return new ExecutionFlowBuilder<>(StepFlow::new);
        }
    }
}
