package se.tink.backend.aggregation.agents.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.modules.providers.LegacyAgentProductionStrategy;
import se.tink.backend.aggregation.agents.modules.providers.LegacyAgentStrategyInterface;

public final class LegacyAgentProductionStrategyModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LegacyAgentStrategyInterface.class)
                .to(LegacyAgentProductionStrategy.class)
                .in(Scopes.SINGLETON);
    }
}
