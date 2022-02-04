package se.tink.agent.runtime.environment;

import lombok.AllArgsConstructor;
import se.tink.agent.sdk.environment.Operation;
import se.tink.agent.sdk.environment.Utilities;

@AllArgsConstructor
public class AgentEnvironment {
    private final Operation operation;
    private final Utilities utilities;

    public Operation getOperation() {
        return operation;
    }

    public Utilities getUtilities() {
        return utilities;
    }
}
