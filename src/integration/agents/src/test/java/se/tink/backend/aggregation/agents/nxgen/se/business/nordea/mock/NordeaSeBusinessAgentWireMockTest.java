package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.mock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class NordeaSeBusinessAgentWireMockTest {
    @Test
    public void testRefresh() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/business/nordea/mock/resources/nordea_business.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/business/nordea/mock/resources/agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.SE, "nordea-business-bankid", wireMockFilePath)
                        .addCredentialField("psu-corporate-id", "1111111111")
                        .addCredentialField("SSN", "111111111112")
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.CHECKING_TRANSACTIONS)
                        .addRefreshableItems(RefreshableItem.SAVING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.SAVING_TRANSACTIONS)
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
