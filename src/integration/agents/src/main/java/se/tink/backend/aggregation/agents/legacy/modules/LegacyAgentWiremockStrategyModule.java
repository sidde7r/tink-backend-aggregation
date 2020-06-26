package se.tink.backend.aggregation.agents.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.modules.providers.LegacyAgentStrategyInterface;
import se.tink.backend.aggregation.agents.modules.providers.LegacyAgentWiremockStrategy;

public final class LegacyAgentWiremockStrategyModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LegacyAgentStrategyInterface.class)
                .to(LegacyAgentWiremockStrategy.class)
                .in(Scopes.SINGLETON);
    }
}
