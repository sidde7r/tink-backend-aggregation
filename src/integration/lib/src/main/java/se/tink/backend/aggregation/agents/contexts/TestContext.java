package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;

public interface TestContext {
    boolean isTestContext();

    void setTestContext(boolean isTestContext);

    AgentsServiceConfiguration getConfiguration();

    void setConfiguration(AgentsServiceConfiguration configuration);
}
