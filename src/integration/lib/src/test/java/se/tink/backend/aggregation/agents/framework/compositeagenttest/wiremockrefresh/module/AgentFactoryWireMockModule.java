package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.module;

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
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.WireMockCommandSequence;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.module.AgentWireMockModuleFactory;

public final class AgentFactoryWireMockModule extends AbstractModule {

    private final FakeBankSocket fakeBankSocket;
    private final Map<String, String> callbackData;
    private final Module agentModule;

    public AgentFactoryWireMockModule(
            FakeBankSocket fakeBankSocket, Map<String, String> callbackData, Module agentModule) {
        this.fakeBankSocket = fakeBankSocket;
        this.callbackData = callbackData;
        this.agentModule = agentModule;
    }

    @Override
    protected void configure() {

        // TODO: Replace WireMockConfiguration, currently needed for AgentWireMockModuleFactory
        bind(FakeBankSocket.class).toInstance(fakeBankSocket);
        bind(WireMockConfiguration.class)
                .toInstance(
                        WireMockConfiguration.builder()
                                .setCallbackData(callbackData)
                                .setAgentModule(agentModule)
                                .build());
        bind(AgentModuleFactory.class).to(AgentWireMockModuleFactory.class).in(Scopes.SINGLETON);
        bind(AgentFactory.class).to(AgentFactoryImpl.class).in(Scopes.SINGLETON);
        bind(Agent.class).toProvider(AgentProvider.class).in(Scopes.SINGLETON);
        bind(CompositeAgentTestCommandSequence.class)
                .to(WireMockCommandSequence.class)
                .in(Scopes.SINGLETON);
    }
}
