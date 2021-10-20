package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.integration;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class FortisAgentWireMockTest {

    @Test
    public void testRefreshWithoutAuthentication() throws Exception {

        // given
        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName("be-fortis-cardreader")
                        .withWireMockFilePath(path("refresh-manual-skip-authentication.aap"))
                        .withoutConfigFile()
                        .skipAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .build();

        // and
        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(path("agent-contract.json"));

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    private String path(String filename) {
        return String.format(
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/fortis/integration/resources/%s",
                filename);
    }
}
