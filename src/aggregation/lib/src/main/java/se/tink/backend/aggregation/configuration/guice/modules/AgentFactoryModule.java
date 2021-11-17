package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentFactoryImpl;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentModuleFactory;
import se.tink.backend.aggregation.agents.module.factory.AgentPackageModuleFactory;
import se.tink.backend.aggregation.agents.module.loader.AgentDependencyModuleLoader;
import se.tink.backend.aggregation.agents.module.loader.AgentDependencyModuleLoaderForProduction;

public final class AgentFactoryModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AgentDependencyModuleLoader.class)
                .to(AgentDependencyModuleLoaderForProduction.class)
                .in(Scopes.SINGLETON);
        bind(AgentModuleFactory.class).to(AgentPackageModuleFactory.class).in(Scopes.SINGLETON);
        bind(AgentFactory.class).to(AgentFactoryImpl.class).in(Scopes.SINGLETON);
    }
}
