package se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentContextProviderImpl implements AgentContextProvider {

    private final CredentialsRequest credentialsRequest;
    private final CompositeAgentContext context;

    @Inject
    public AgentContextProviderImpl(
            CredentialsRequest credentialsRequest, CompositeAgentContext context) {
        this.credentialsRequest = credentialsRequest;
        this.context = context;
    }

    @Override
    public CredentialsRequest getCredentialsRequest() {
        return credentialsRequest;
    }

    @Override
    public CompositeAgentContext getContext() {
        return context;
    }

    @Override
    public MetricContext getMetricContext() {
        return context;
    }

    @Override
    public SystemUpdater getSystemUpdater() {
        return context;
    }

    @Override
    public AgentAggregatorIdentifier getAgentAggregatorIdentifier() {
        return context;
    }

    @Override
    public ProviderSessionCacheContext getProviderSessionCacheContext() {
        return context;
    }
}
