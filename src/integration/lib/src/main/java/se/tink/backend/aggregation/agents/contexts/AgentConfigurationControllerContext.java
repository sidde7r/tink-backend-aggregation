package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;

public interface AgentConfigurationControllerContext {

    AgentConfigurationControllerable getAgentConfigurationController();

    void setAgentConfigurationController(
            AgentConfigurationControllerable agentConfigurationController);
}
