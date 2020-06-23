package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.mock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class HandelsbankenObMockServerAgentTest {

    private final String mockedSsn = "190000000000";

    private final String CONFIGURATION_PATH =
            "data/agents/openbanking/handelsbanken/configuration.yml";

    final String wireMockServerFilePath =
            "data/agents/openbanking/handelsbanken/HB_OB_wireMock.aap";

    @Test
    public void testRefresh() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.SE, "se-handelsbanken-ob", wireMockServerFilePath)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), mockedSsn)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withConfigurationFile(configuration)
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();
    }
}
