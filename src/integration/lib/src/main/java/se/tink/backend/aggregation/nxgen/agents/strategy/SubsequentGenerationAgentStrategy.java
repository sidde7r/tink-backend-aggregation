package se.tink.backend.aggregation.nxgen.agents.strategy;

import se.tink.backend.aggregation.agents.CompositeAgentContext;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * This interface exists to separate the agent's behavior from its dependency resolution. Put
 * simply, it allows you to set every field of the SubsequentGenerationAgent agent parent class to
 * anything you desire.
 *
 * <p>Some guidelines for classes implementing this interface to prevent misuse: The class should be
 * unmodifiable. The class should be final. Every field of the class should be final.
 */
public interface SubsequentGenerationAgentStrategy {

    TinkHttpClient getTinkHttpClient();

    CredentialsRequest getCredentialsRequest();

    CompositeAgentContext getContext();

    SuperAbstractAgentStrategy getSuperAbstractAgentStrategy();
}
