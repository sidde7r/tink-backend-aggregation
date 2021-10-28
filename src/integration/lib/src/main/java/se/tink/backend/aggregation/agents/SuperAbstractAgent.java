package se.tink.backend.aggregation.agents;

import java.util.Optional;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLogger;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * Same as the former AbstractAgent, minus the members and methods that are only used by legacy
 * agents.
 */
public abstract class SuperAbstractAgent implements Agent, AgentEventListener {
    protected AgentsServiceConfiguration configuration;
    protected final CompositeAgentContext context;
    private final AgentAggregatorIdentifier agentAggregatorIdentifier;

    protected final ProviderSessionCacheContext providerSessionCacheContext;
    protected final SystemUpdater systemUpdater;
    protected final MetricContext metricContext;
    protected final CredentialsRequest request;
    protected final LogMasker logMasker;

    protected SuperAbstractAgent(final AgentContextProvider agentContextProvider) {
        this.request = agentContextProvider.getCredentialsRequest();
        this.context = agentContextProvider.getContext();
        this.agentAggregatorIdentifier = agentContextProvider.getAgentAggregatorIdentifier();
        this.providerSessionCacheContext = agentContextProvider.getProviderSessionCacheContext();
        this.systemUpdater = agentContextProvider.getSystemUpdater();
        this.metricContext = agentContextProvider.getMetricContext();
        this.logMasker = agentContextProvider.getContext().getLogMasker();
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

    public LogMasker getLogMasker() {
        return logMasker;
    }

    /**
     * Note that HTTP JSON logs will be empty unless you use {@link NextGenTinkHttpClient} with
     * {@link LoggingStrategy#EXPERIMENTAL} (or when you use {@link JsonHttpTrafficLogger} directly
     * in your own logging solution).
     */
    public void setJsonHttpTrafficLogsEnabled(boolean enabled) {
        Optional.ofNullable(context.getJsonHttpTrafficLogger())
                .ifPresent(logger -> logger.setEnabled(enabled));
    }
}
