package se.tink.backend.aggregation.agents;

import se.tink.backend.common.config.ServiceConfiguration;

public interface Agent {

    void setConfiguration(ServiceConfiguration configuration);

    Class<? extends Agent> getAgentClass();
    
    boolean login() throws Exception;

    void logout() throws Exception;

    // Clean up resources. No further operations should be done on the agent after this.
    void close();
}
