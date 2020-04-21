package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import java.util.Map;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.AgentFactoryImpl;
import se.tink.backend.aggregation.agents.agentfactory.AgentModuleFactory;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommandSequence;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.provider.AgentProvider;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.module.AgentWireMockModuleFactory;

public final class AgentFactoryWireMockModule extends AbstractModule {

    private final int port;
    private final Map<String, String> callbackData;
    private final Module agentModule;
    private final Class<? extends CompositeAgentTestCommandSequence> commandSequence;

    public AgentFactoryWireMockModule(
            int port,
            Map<String, String> callbackData,
            Module agentModule,
            Class<? extends CompositeAgentTestCommandSequence> commandSequence) {
        this.port = port;
        this.callbackData = callbackData;
        this.agentModule = agentModule;
        this.commandSequence = commandSequence;
    }

    @Override
    protected void configure() {

        // TODO: Replace WireMockConfiguration, currently needed for AgentWireMockModuleFactory
        bind(WireMockConfiguration.class)
                .toInstance(
                        WireMockConfiguration.builder("localhost:" + port)
                                .setCallbackData(callbackData)
                                .setAgentModule(agentModule)
                                .build());
        bind(AgentModuleFactory.class).to(AgentWireMockModuleFactory.class).in(Scopes.SINGLETON);
        bind(AgentFactory.class).to(AgentFactoryImpl.class).in(Scopes.SINGLETON);
        bind(Agent.class).toProvider(AgentProvider.class).in(Scopes.SINGLETON);
        bind(CompositeAgentTestCommandSequence.class).to(commandSequence).in(Scopes.SINGLETON);
    }
}
