package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step;

import java.util.Set;

public interface WireMockConfigurationStep {

    AgentsServiceConfigurationStep withWireMockFilePath(String wireMockFilePath);

    AgentsServiceConfigurationStep withWireMockFilePaths(Set<String> wireMockFilePaths);
}
