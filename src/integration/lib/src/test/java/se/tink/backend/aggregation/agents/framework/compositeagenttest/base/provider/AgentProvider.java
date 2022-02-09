package se.tink.backend.aggregation.agents.framework.compositeagenttest.base.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentProvider extends AbstractModule {

    @Singleton
    @Provides
    public AgentInstance provideAgentSdkInstance(
            AgentFactory agentFactory, CredentialsRequest request, AgentContext context) {
        try {
            return agentFactory.createAgentSdkInstance(request, context);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Singleton
    @Provides
    public Agent provideAgent(AgentInstance agentInstance) {
        return agentInstance.instanceOf(Agent.class).orElse(null);
    }
}
