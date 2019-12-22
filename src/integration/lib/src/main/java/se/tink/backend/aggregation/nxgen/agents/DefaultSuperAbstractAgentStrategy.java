package se.tink.backend.aggregation.nxgen.agents;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class DefaultSuperAbstractAgentStrategy implements SuperAbstractAgentStrategy {

    private final CredentialsRequest credentialsRequest;
    private final AgentContext context;

    public DefaultSuperAbstractAgentStrategy(
            final CredentialsRequest credentialsRequest, final AgentContext context) {
        this.credentialsRequest = credentialsRequest;
        this.context = context;
    }

    @Override
    public CredentialsRequest getCredentialsRequest() {
        return credentialsRequest;
    }

    @Override
    public AgentContext getContext() {
        return context;
    }

    @Override
    public AgentAggregatorIdentifier getAgentAggregatorIdentifier() {
        return context;
    }

    @Override
    public SupplementalRequester getSupplementalRequester() {
        return context;
    }

    @Override
    public ProviderSessionCacheContext getProviderSessionCacheContext() {
        return context;
    }

    @Override
    public SystemUpdater getSystemUpdater() {
        return context;
    }

    @Override
    public MetricContext getMetricContext() {
        return context;
    }
}
