package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.mock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class DanskeBankSEWireMockAgentTest {
    private static final String PSU = "199901011230";

    @Test
    public void testRefresh() throws Exception {
        // given
        final String wireMockFilePath = "data/agents/se/danskebank/se-danskebank-bankid-mock.aap";
        final String contractFilePath = "data/agents/se/danskebank/danskebank-agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.SE, "danskebank-bankid", wireMockFilePath)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), PSU)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
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
