package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step;

import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public interface AgentsServiceConfigurationStep {

    /**
     * Use specified AgentsServiceConfiguration for agent.
     *
     * @param configuration agent service configuration.
     * @return This builder.
     */
    AuthenticationConfigurationStep withConfigFile(AgentsServiceConfiguration configuration);

    AuthenticationConfigurationStep withoutConfigFile();
}
