package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.workers.commands.state.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandDefaultState;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation;

public class AgentWorkerCommandModule extends AbstractModule {
    @Override
    protected void configure() {
        // Command States
        bind(AgentWorkerOperation.AgentWorkerOperationState.class).in(Scopes.SINGLETON);
        bind(DebugAgentWorkerCommandState.class).in(Scopes.SINGLETON);
        bind(CircuitBreakerAgentWorkerCommandState.class).in(Scopes.SINGLETON);
        bind(InstantiateAgentWorkerCommandState.class)
                .to(InstantiateAgentWorkerCommandDefaultState.class)
                .in(Scopes.SINGLETON);
        bind(LoginAgentWorkerCommandState.class).in(Scopes.SINGLETON);
        bind(ReportProviderMetricsAgentWorkerCommandState.class).in(Scopes.SINGLETON);
    }
}
