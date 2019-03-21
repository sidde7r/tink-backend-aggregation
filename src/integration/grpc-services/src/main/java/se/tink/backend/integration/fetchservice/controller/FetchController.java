package se.tink.backend.integration.fetchservice.controller;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.grpc.FakeIntegrationArgumentsCreator;
import se.tink.libraries.metrics.MetricRegistry;

public class FetchController {
    private final AgentFactory agentFactory;
    private final MetricRegistry metricRegistry;

    @Inject
    FetchController(AgentFactory agentFactory, MetricRegistry metricRegistry) {
        this.agentFactory = agentFactory;
        this.metricRegistry = metricRegistry;
    }

    public FetchAccountsResponse execute(FetchCheckingAccountsCommand command) throws Exception {
        RefreshCheckingAccountsExecutor agent = (RefreshCheckingAccountsExecutor) agentFactory.createForIntegration(
                command.getAgentInfo().getAgentClassName(),
                FakeIntegrationArgumentsCreator.getCredReq(),
                FakeIntegrationArgumentsCreator.getAgentContext(metricRegistry)
        );
        return agent.fetchCheckingAccounts();
    }

}

