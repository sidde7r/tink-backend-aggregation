package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step;

import java.util.Set;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;

public interface WireMockConfigurationStep {

    AgentsServiceConfigurationStep withWireMockServer(WireMockTestServer wireMockServer);

    AgentsServiceConfigurationStep withWireMockFilePath(String wireMockFilePath);

    AgentsServiceConfigurationStep withWireMockFilePaths(Set<String> wireMockFilePaths);
}
