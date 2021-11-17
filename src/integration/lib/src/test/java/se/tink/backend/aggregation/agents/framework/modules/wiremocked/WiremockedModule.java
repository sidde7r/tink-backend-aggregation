package se.tink.backend.aggregation.agents.framework.modules.wiremocked;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentFactoryImpl;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentModuleFactory;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.module.AgentWireMockModuleFactory;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public class WiremockedModule extends AbstractModule {

    private final WireMockConfiguration wireMockConfiguration;
    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final FakeBankSocket fakeBankSocket;

    public WiremockedModule(
            WireMockConfiguration wireMockConfiguration,
            AgentsServiceConfiguration agentsServiceConfiguration,
            FakeBankSocket fakeBankSocket) {
        this.wireMockConfiguration = wireMockConfiguration;
        this.agentsServiceConfiguration = agentsServiceConfiguration;
        this.fakeBankSocket = fakeBankSocket;
    }

    public void configure() {
        bind(AgentFactory.class).to(AgentFactoryImpl.class);
        bind(AgentModuleFactory.class).to(AgentWireMockModuleFactory.class);
        bind(WireMockConfiguration.class).toInstance(wireMockConfiguration);
        bind(AgentsServiceConfiguration.class).toInstance(agentsServiceConfiguration);

        bind(FakeBankSocket.class).toInstance(fakeBankSocket);
    }
}
