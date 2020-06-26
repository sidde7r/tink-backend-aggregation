package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.mock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class AmexObMockServerAgentTest {

    private static final String CONFIGURATION_PATH =
            "data/agents/openbanking/amex/configuration.yml";

    private static final String WIREMOCK_SERVER_FILEPATH =
            "data/agents/openbanking/amex/amex_OB_wireMock.aap";

    private static final String CONTRACT_FILE_PATH =
            "data/agents/openbanking/amex/agent-contract.json";

    @Test
    public void testRefresh() throws Exception {

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.SE, "se-amex-ob", WIREMOCK_SERVER_FILEPATH)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("authtoken", "dummyCode")
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
