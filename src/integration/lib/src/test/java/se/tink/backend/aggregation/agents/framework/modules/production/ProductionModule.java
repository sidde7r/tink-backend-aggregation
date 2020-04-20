package se.tink.backend.aggregation.agents.framework.modules.production;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.agentfactory.AgentFactoryImpl;
import se.tink.backend.aggregation.agents.agentfactory.AgentModuleFactory;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.module.factory.AgentPackageModuleFactory;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public class ProductionModule extends AbstractModule {

    private final AgentsServiceConfiguration agentsServiceConfiguration;

    public ProductionModule(AgentsServiceConfiguration agentsServiceConfiguration) {
        this.agentsServiceConfiguration = agentsServiceConfiguration;
    }

    public void configure() {
        bind(AgentFactory.class).to(AgentFactoryImpl.class);
        bind(AgentModuleFactory.class).to(AgentPackageModuleFactory.class);
        bind(AgentsServiceConfiguration.class).toInstance(agentsServiceConfiguration);
    }
}
