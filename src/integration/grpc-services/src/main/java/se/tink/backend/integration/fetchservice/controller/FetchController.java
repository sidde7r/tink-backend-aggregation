package se.tink.backend.integration.fetchservice.controller;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentClassFactory;
import se.tink.backend.aggregation.agents.grpc.FakeIntegrationArgumentsCreator;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class FetchController {
    private final AgentFactory agentFactory;
    private final MetricRegistry metricRegistry;

    @Inject
    FetchController(AgentFactory agentFactory, MetricRegistry metricRegistry) {
        this.agentFactory = agentFactory;
        this.metricRegistry = metricRegistry;
    }

    public FetchAccountsResponse execute(FetchCheckingAccountsCommand command) throws Exception {
        Class<? extends Agent> agentClass =
                AgentClassFactory.getAgentClass(command.getAgentInfo().getAgentClassName());
        RefreshCheckingAccountsExecutor agent =
                (RefreshCheckingAccountsExecutor)
                        agentFactory.create(
                                agentClass,
                                FakeIntegrationArgumentsCreator.getCredReq(),
                                FakeIntegrationArgumentsCreator.getAgentContext(metricRegistry));
        return agent.fetchCheckingAccounts();
    }
}
