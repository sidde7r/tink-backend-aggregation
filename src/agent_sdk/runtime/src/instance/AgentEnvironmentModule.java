package src.agent_sdk.runtime.src.instance;

import com.google.inject.AbstractModule;
import se.tink.agent.sdk.environment.Operation;
import se.tink.agent.sdk.environment.Utilities;
import src.agent_sdk.runtime.src.environment.AgentEnvironment;

public class AgentEnvironmentModule extends AbstractModule {
    private final AgentEnvironment environment;

    public AgentEnvironmentModule(AgentEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(Operation.class).toInstance(this.environment.getOperation());
        bind(Utilities.class).toInstance(this.environment.getUtilities());
    }
}
