package se.tink.agent.sdk.steppable_execution.execution_flow;

public class DuplicateStepException extends RuntimeException {
    public DuplicateStepException(String stepId) {
        super(String.format("Duplicate execution step added: '%s'.", stepId));
    }
}
