package se.tink.backend.aggregation.agents.module.agentclass;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import se.tink.backend.aggregation.agents.agent.Agent;

public final class AgentClassModule extends AbstractModule {

    private final Class<? extends Agent> agentClass;

    /**
     * Binds the provided class with guice with @AgentClass annotation.
     *
     * <p>For injection both <code>@AgentClass Class</code> and <code>
     * @AgentClass Class<? extends Agent></?></code> can be used.
     *
     * @param agentClass The agent class to be bound.
     */
    public AgentClassModule(Class<? extends Agent> agentClass) {
        this.agentClass = agentClass;
    }

    @Override
    protected void configure() {
        bind(Class.class).annotatedWith(AgentClass.class).toInstance(agentClass);
        bind(new TypeLiteral<Class<? extends Agent>>() {})
                .annotatedWith(AgentClass.class)
                .toInstance(agentClass);
    }
}
