package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.aggregation.agents.AgentModuleFactory;
import se.tink.backend.aggregation.agents.module.factory.AgentPackageModuleFactory;

public final class AgentFactoryModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(AgentModuleFactory.class).to(AgentPackageModuleFactory.class).in(Scopes.SINGLETON);
        bind(AgentFactory.class).in(Scopes.SINGLETON);
    }
}
