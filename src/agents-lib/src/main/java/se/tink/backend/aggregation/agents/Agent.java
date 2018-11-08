package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;

public interface Agent {

    void setConfiguration(AgentsServiceConfiguration configuration);

    Class<? extends Agent> getAgentClass();
    
    boolean login() throws Exception;

    void logout() throws Exception;

    // Clean up resources. No further operations should be done on the agent after this.
    void close();
}
