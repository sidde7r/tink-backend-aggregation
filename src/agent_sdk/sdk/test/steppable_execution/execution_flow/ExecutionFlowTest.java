package src.agent_sdk.sdk.test.steppable_execution.execution_flow;

import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequestBase;
import se.tink.agent.sdk.steppable_execution.execution_flow.DuplicateStepException;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowBuilder;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowImpl;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.InteractiveFlowStartStep;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.NonInteractiveFlowStartStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.IntermediateStepResponse;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractionStepResponse;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractiveStep;

public class ExecutionFlowTest {
    @Test(expected = DuplicateStepException.class)
    public void interactiveFlowDuplicateSteps() {
        InteractiveFlow.builder().startStep(new InteractiveStepA()).addStep(new InteractiveStepA());
    }

    @Test(expected = DuplicateStepException.class)
    public void nonInteractiveFlowDuplicateSteps() {
        NonInteractiveFlow.builder()
                .startStep(new NonInteractiveStepA())
                .addStep(new NonInteractiveStepA());
    }

    @Test
    public void interactiveFlowWithIntermediateStep() {
        InteractiveStep<Void, Void> stepA = new InteractiveStepA();
        InteractiveStep<Void, Void> stepB = new InteractiveStepB();
        IntermediateStep stepC = new InteractiveStepC();

        ExecutionFlow<Void, Void> flow =
                InteractiveFlow.builder().startStep(stepA).addStep(stepB).addStep(stepC).build();

        Optional<BaseStep<Void, Void>> shouldBeStepC =
                flow.getStep(InteractiveStepC.class.toString());
        Assert.assertEquals(Optional.of(stepC), shouldBeStepC);
    }

    @Test
    public void nullStepIdShouldReturnStartStep() {
        InteractiveStep<Void, Void> stepA = new InteractiveStepA();
        InteractiveStep<Void, Void> stepB = new InteractiveStepB();

        ExecutionFlow<Void, Void> flow =
                InteractiveFlow.builder().startStep(stepA).addStep(stepB).build();

        Optional<BaseStep<Void, Void>> startStep = flow.getStep(null);
        Assert.assertEquals(Optional.of(stepA), startStep);
    }

    private static class InteractiveFlow extends ExecutionFlowImpl<Void, Void> {
        private InteractiveFlow(String startStepId, Map<String, BaseStep<Void, Void>> steps) {
            super(startStepId, steps);
        }

        public static InteractiveFlowStartStep<Void, Void, InteractiveFlow> builder() {
            return new ExecutionFlowBuilder<>(InteractiveFlow::new);
        }
    }

    private static class NonInteractiveFlow extends ExecutionFlowImpl<Void, Void> {
        private NonInteractiveFlow(String startStepId, Map<String, BaseStep<Void, Void>> steps) {
            super(startStepId, steps);
        }

        public static NonInteractiveFlowStartStep<Void, Void, NonInteractiveFlow> builder() {
            return new ExecutionFlowBuilder<>(NonInteractiveFlow::new);
        }
    }

    private static class InteractiveStepA extends InteractiveStep<Void, Void> {
        @Override
        public InteractiveStepResponse<Void> execute(StepRequest<Void> request) {
            return null;
        }
    }

    private static class InteractiveStepB extends InteractiveStep<Void, Void> {
        @Override
        public InteractiveStepResponse<Void> execute(StepRequest<Void> request) {
            return null;
        }
    }

    private static class InteractiveStepC extends IntermediateStep {
        @Override
        public IntermediateStepResponse execute(StepRequest<Void> request) {
            return null;
        }
    }

    private static class NonInteractiveStepA extends NonInteractiveStep<Void, Void> {
        @Override
        public NonInteractionStepResponse<Void> execute(StepRequestBase<Void> request) {
            return null;
        }
    }
}
