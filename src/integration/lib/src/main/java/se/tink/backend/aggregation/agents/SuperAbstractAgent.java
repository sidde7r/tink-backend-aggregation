package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * Same as the former AbstractAgent, minus the members and methods that are only used by legacy
 * agents.
 */
public abstract class SuperAbstractAgent implements Agent, AgentEventListener {
    protected AgentsServiceConfiguration configuration;
    protected final AgentContext context;
    private final AgentAggregatorIdentifier agentAggregatorIdentifier;
    protected final SupplementalRequester supplementalRequester;
    protected final SystemUpdater systemUpdater;
    protected final MetricContext metricContext;
    protected final CredentialsRequest request;

    protected SuperAbstractAgent(CredentialsRequest request, AgentContext context) {
        this.request = request;
        this.context = context;
        this.agentAggregatorIdentifier = context;
        this.supplementalRequester = context;
        this.systemUpdater = context;
        this.metricContext = context;
    }

    public final AggregatorInfo getAggregatorInfo() {
        return agentAggregatorIdentifier.getAggregatorInfo();
    }

    @Override
    public final Class<? extends Agent> getAgentClass() {
        return getClass();
    }

    /**
     * Default is to do nothing here, updated timestamp is set in updateService if status goes to
     * UPDATED.
     */
    @Override
    public final void onUpdateCredentialsStatus() {
        // Nothing.
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public final void close() {
        // Deliberately left empty. Feel free to override for agents that need proper cleanup of
        // resources.
    }
}
