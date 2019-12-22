package se.tink.backend.aggregation.nxgen.agents.strategy;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * This interface exists to separate the agent's behavior from its dependency resolution. Put
 * simply, it allows you to set every field of the SuperAbstractAgent agent ancestor class to
 * anything you desire.
 *
 * <p>Some guidelines for classes implementing this interface to prevent misuse: The class should be
 * unmodifiable. The class should be final. Every field of the class should be final.
 */
public interface SuperAbstractAgentStrategy {

    CredentialsRequest getCredentialsRequest();

    AgentContext getContext();

    AgentAggregatorIdentifier getAgentAggregatorIdentifier();

    SupplementalRequester getSupplementalRequester();

    ProviderSessionCacheContext getProviderSessionCacheContext();

    SystemUpdater getSystemUpdater();

    MetricContext getMetricContext();
}
