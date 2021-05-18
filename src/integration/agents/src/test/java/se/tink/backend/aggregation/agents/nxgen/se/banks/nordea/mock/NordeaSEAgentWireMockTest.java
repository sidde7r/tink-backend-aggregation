package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.mock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class NordeaSEAgentWireMockTest {

    @Test
    public void testRefresh() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordea_mock_refresh_log.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.SE)
                        .withProviderName("nordea-bankid")
                        .withWireMockFilePath(wireMockFilePath)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
