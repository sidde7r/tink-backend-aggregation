package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class AmexV62UkMockServerWithContractFileAgentTest {

    private final String USERNAME = "testuser";
    private final String PASSWORD = "testPassword";

    @Test
    public void testRefreshWithJSONContractFile() throws Exception {
        // given
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.UK,
                                "uk-americanexpress-password",
                                "data/agents/uk/amex/refresh-traffic.aap")
                        .addCredentialField(Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Key.PASSWORD.getFieldKey(), PASSWORD)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile("data/agents/uk/amex/agent-contract.json");

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
