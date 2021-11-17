package se.tink.backend.aggregation.agents.framework.modules.production;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentFactoryImpl;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentModuleFactory;
import se.tink.backend.aggregation.agents.module.factory.AgentPackageModuleFactory;
import se.tink.backend.aggregation.agents.module.loader.AgentDependencyModuleLoader;
import se.tink.backend.aggregation.agents.module.loader.AgentDependencyModuleLoaderForProduction;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public class AgentIntegrationTestModule extends AbstractModule {

    private final AgentsServiceConfiguration agentsServiceConfiguration;

    public AgentIntegrationTestModule(AgentsServiceConfiguration agentsServiceConfiguration) {
        this.agentsServiceConfiguration = agentsServiceConfiguration;
    }

    public void configure() {
        bind(AgentFactory.class).to(AgentFactoryImpl.class);
        bind(AgentModuleFactory.class).to(AgentPackageModuleFactory.class);
        bind(AgentsServiceConfiguration.class).toInstance(agentsServiceConfiguration);
        bind(AgentDependencyModuleLoader.class)
                .to(AgentDependencyModuleLoaderForProduction.class)
                .in(Scopes.SINGLETON);
    }
}
