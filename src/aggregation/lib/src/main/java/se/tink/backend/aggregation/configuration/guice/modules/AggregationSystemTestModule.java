package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.agentfactory.AgentModuleFactory;

public class AggregationSystemTestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AgentModuleFactory.class).to(AgentSystemTestModuleFactory.class).in(Scopes.SINGLETON);
    }
}
