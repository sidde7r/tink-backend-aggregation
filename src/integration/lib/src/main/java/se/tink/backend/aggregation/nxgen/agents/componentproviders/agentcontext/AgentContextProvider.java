package se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext;

import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.libraries.credentials.service.CredentialsRequest;

public interface AgentContextProvider {

    CredentialsRequest getCredentialsRequest();

    CompositeAgentContext getContext();

    MetricContext getMetricContext();

    SystemUpdater getSystemUpdater();

    AgentAggregatorIdentifier getAgentAggregatorIdentifier();

    ProviderSessionCacheContext getProviderSessionCacheContext();
}
