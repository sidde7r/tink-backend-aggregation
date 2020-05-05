package se.tink.backend.aggregation.agents.module.agentclass.testobject;

import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public final class TestAgentClass implements Agent {

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {}

    @Override
    public Class<? extends Agent> getAgentClass() {
        return null;
    }

    @Override
    public boolean login() throws Exception {
        return false;
    }

    @Override
    public void logout() throws Exception {}

    @Override
    public void close() {}
}
