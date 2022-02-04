package se.tink.agent.runtime.test.steppable_execution;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.runtime.steppable_execution.SteppableExecutor;
import se.tink.agent.runtime.user_interaction.UserResponseDataImpl;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowBuilder;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowImpl;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.InteractiveFlowStartStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.user_interaction.UserInteractionType;
import se.tink.agent.sdk.user_interaction.UserResponseData;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SteppableExecutorTest {

    @Test(expected = SteppableExecutor.StepNotFoundException.class)
    public void testStepNotFound() {
        StepFlow stepFlow = StepFlow.builder().startStep(new StepA()).build();

        SteppableExecutor<String, Integer> executor = new SteppableExecutor<>(stepFlow);

        StepRequest<String> request = new StepRequest<>("foo", null, null, null);
        executor.execute(null, request);
    }

    @Test
    public void testUserInteractionRequest() {
        StepFlow stepFlow = StepFlow.builder().startStep(new StepA()).addStep(new StepB()).build();

        SteppableExecutor<String, Integer> executor = new SteppableExecutor<>(stepFlow);

        StepRequest<String> request = new StepRequest<>("foo", null, null, null);
        StepResponse<Integer> response = executor.execute(null, request);

        Assert.assertEquals(Optional.of(StepC.class.toString()), response.getNextStepId());
        Assert.assertTrue(response.getUserInteraction().isPresent());
        Assert.assertEquals(
                UserInteractionType.THIRD_PARTY_APP, response.getUserInteraction().get().getType());
    }

    @Test
    public void testFullFlow() {
        StepFlow stepFlow =
                StepFlow.builder()
                        .startStep(new StepA())
                        .addStep(new StepB())
                        .addStep(new StepC())
                        .build();

        SteppableExecutor<String, Integer> executor = new SteppableExecutor<>(stepFlow);

        StepRequest<String> request = new StepRequest<>("foo", null, null, null);
        StepResponse<Integer> response = executor.execute(null, request);

        // Feed back a user response
        Map<String, String> rawUserResponseData = new HashMap<>();
        rawUserResponseData.put("foo", "bar");
        UserResponseData userResponseData = new UserResponseDataImpl(rawUserResponseData);
        request = new StepRequest<>("foo", null, null, userResponseData);

        response = executor.execute(response.getNextStepId().get(), request);

        Assert.assertEquals(Optional.of(123), response.getDonePayload());
    }

    @Test
    public void testMaxExecutionTime() {
        StepFlow stepFlow =
                StepFlow.builder().startStep(new LongRunningStep()).addStep(new StepA()).build();

        // Allow maximum execution time of 1ms.
        SteppableExecutor<String, Integer> executor =
                new SteppableExecutor<>(Duration.ofMillis(1), stepFlow);

        StepRequest<String> request = new StepRequest<>("foo", null, null, null);
        StepResponse<Integer> response = executor.execute(null, request);

        // Only one step, `LongRunningStep`, should have had time to execute.
        Assert.assertEquals(Optional.of(StepA.class.toString()), response.getNextStepId());
    }

    private static class LongRunningStep extends InteractiveStep<String, Integer> {
        @Override
        @SuppressWarnings("java:S2925")
        public InteractiveStepResponse<Integer> execute(StepRequest<String> request) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Noop.
            }
            return InteractiveStepResponse.nextStep(StepA.class).noUserInteraction().build();
        }
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
