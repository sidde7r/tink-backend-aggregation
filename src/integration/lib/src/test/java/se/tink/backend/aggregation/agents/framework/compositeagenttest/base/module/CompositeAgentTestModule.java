package se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTest;

public final class CompositeAgentTestModule extends AbstractModule {

    private final Class<? extends Provider<CompositeAgentTest>> compositeAgentTestProviderClass;

    public CompositeAgentTestModule(
            Class<? extends Provider<CompositeAgentTest>> compositeAgentTestProviderClass) {
        this.compositeAgentTestProviderClass = compositeAgentTestProviderClass;
    }

    @Override
    protected void configure() {
        bind(CompositeAgentTest.class)
                .toProvider(compositeAgentTestProviderClass)
                .in(Scopes.SINGLETON);
    }
}
