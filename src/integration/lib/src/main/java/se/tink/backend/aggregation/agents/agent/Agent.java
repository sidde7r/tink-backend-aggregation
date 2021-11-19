package se.tink.backend.aggregation.agents.agent;

import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account_data_cache.AccountDataCache;

public interface Agent {

    /**
     * @deprecated Use the constructor with parameter list (CredentialsRequest, AgentContext,
     *     AgentsServiceConfiguration) for your agent instead. Use {@link
     *     AgentsServiceConfiguration#getSignatureKeyPair()} to retrieve the SignatureKeyPair.
     */
    @Deprecated
    void setConfiguration(AgentsServiceConfiguration configuration);

    Class<? extends Agent> getAgentClass();

    boolean login() throws Exception;

    void logout() throws Exception;

    // Clean up resources. No further operations should be done on the agent after this.
    void close();

    default void accept(AgentVisitor visitor) {
        visitor.visit(this);
    }

    default void afterRefreshPostProcess(AccountDataCache cache) {
        // NOOP
    }
}
