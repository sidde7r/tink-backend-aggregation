package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * Same as the former AbstractAgent, minus the members and methods that are only used by legacy
 * agents.
 */
public abstract class SuperAbstractAgent implements Agent, AgentEventListener {
    protected AgentsServiceConfiguration configuration;
    protected final CompositeAgentContext context;
    private final AgentAggregatorIdentifier agentAggregatorIdentifier;

    // FIXME: remove SupplementalRequester completely from here. No agent should use this API
    // FIXME: instead use SupplementalInformationController (exists in SubsequentGenerationAgent)
    protected final SupplementalRequester supplementalRequester;
    protected final ProviderSessionCacheContext providerSessionCacheContext;
    protected final SystemUpdater systemUpdater;
    protected final MetricContext metricContext;
    protected final CredentialsRequest request;

    protected SuperAbstractAgent(final AgentContextProvider agentContextProvider) {
        this.request = agentContextProvider.getCredentialsRequest();
        this.context = agentContextProvider.getContext();
        this.agentAggregatorIdentifier = agentContextProvider.getAgentAggregatorIdentifier();
        this.supplementalRequester = agentContextProvider.getSupplementalRequester();
        this.providerSessionCacheContext = agentContextProvider.getProviderSessionCacheContext();
        this.systemUpdater = agentContextProvider.getSystemUpdater();
        this.metricContext = agentContextProvider.getMetricContext();
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

    public AgentConfigurationControllerable getAgentConfigurationController() {
        return context.getAgentConfigurationController();
    }
}
