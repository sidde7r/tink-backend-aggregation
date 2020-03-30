package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.agentfactory.AgentFactory;
import se.tink.backend.aggregation.agents.agentfactory.AgentModuleFactory;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.WireMockConfigurationProvider;
import se.tink.backend.aggregation.agents.framework.wiremock.module.AgentWireMockModuleFactory;

public final class AgentFactoryWireMockModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(WireMockConfiguration.class)
                .toProvider(WireMockConfigurationProvider.class)
                .in(Scopes.SINGLETON);
        bind(AgentModuleFactory.class).to(AgentWireMockModuleFactory.class).in(Scopes.SINGLETON);
        bind(AgentFactory.class).in(Scopes.SINGLETON);
    }
}
