package se.tink.backend.aggregation.nxgen.agents;

import java.util.Optional;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agent.AgentVisitor;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.handler.AgentPlatformHttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.handler.HttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class LegacyAgentComponentProviderAgentVisitor implements AgentVisitor {

    private PersistentStorage persistentStorage;
    private TinkHttpClient httpClient;
    private HttpResponseStatusHandler legacyResponseStatusHandler;

    @Override
    public void visit(Agent agent) {
        if (agent instanceof SubsequentGenerationAgent) {
            SubsequentGenerationAgent subsequentGenerationAgent = (SubsequentGenerationAgent) agent;
            persistentStorage = subsequentGenerationAgent.getPersistentStorage();
            httpClient = subsequentGenerationAgent.client;
            setAgentPlatformHttpResponseHandler();
        }
    }

    public Optional<PersistentStorage> getPersistentStorage() {
        return Optional.ofNullable(persistentStorage);
    }

    private void setAgentPlatformHttpResponseHandler() {
        legacyResponseStatusHandler = httpClient.getResponseStatusHandler();
        httpClient.setResponseStatusHandler(new AgentPlatformHttpResponseStatusHandler());
    }

    public void doPostAuthenticationLegacyCleaning() {
        httpClient.setResponseStatusHandler(legacyResponseStatusHandler);
    }
}
