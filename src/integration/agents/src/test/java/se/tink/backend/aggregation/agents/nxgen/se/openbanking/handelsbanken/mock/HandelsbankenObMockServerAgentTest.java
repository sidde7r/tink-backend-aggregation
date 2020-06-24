package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.mock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class HandelsbankenObMockServerAgentTest {

    private static final String MOCKED_SSN = "190000000000";

    private static final String CONFIGURATION_PATH =
            "data/agents/openbanking/handelsbanken/configuration.yml";

    private static final String WIREMOCK_SERVER_FILEPATH =
            "data/agents/openbanking/handelsbanken/HB_OB_wireMock.aap";

    private static final String CONTRACT_FILE_PATH =
            "data/agents/openbanking/handelsbanken/agent-contract.json";

    @Test
    public void testRefresh() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.SE, "se-handelsbanken-ob", WIREMOCK_SERVER_FILEPATH)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), MOCKED_SSN)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withConfigurationFile(configuration)
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);

        // When
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
