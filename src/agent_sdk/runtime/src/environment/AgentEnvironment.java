package src.agent_sdk.runtime.src.environment;

import se.tink.agent.sdk.environment.Operation;
import se.tink.agent.sdk.environment.Utilities;

public class AgentEnvironment {
    private final Operation operation;
    private final Utilities utilities;

    public AgentEnvironment(Operation operation, Utilities utilities) {
        this.operation = operation;
        this.utilities = utilities;
    }

    public Operation getOperation() {
        return operation;
    }

    public Utilities getUtilities() {
        return utilities;
    }
}
