package se.tink.backend.aggregation.agents.module.agentclass;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.agent.Agent;

public final class AgentClassModule extends AbstractModule {

    private final Class<? extends Agent> agentClass;

    public AgentClassModule(Class<? extends Agent> agentClass) {
        this.agentClass = agentClass;
    }

    @Override
    protected void configure() {
        bind(Class.class).annotatedWith(AgentClass.class).toInstance(agentClass);
    }
}
