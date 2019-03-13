package se.tink.backend.integration.fetchservice.fetchcontroller;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.integration.api.models.IntegrationAccounts;

public class FetchController {
    private final AgentFactory agentFactory;

    private FetchController(AgentsServiceConfiguration configuration) {
        this.agentFactory = new AgentFactory(configuration);
    }

    public static FetchController create(AgentsServiceConfiguration configuration) {
        return new FetchController(configuration);
    }

    public List<IntegrationAccounts> fetchCheckingAccounts() {
        return Collections.emptyList();
    }
}
