package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.wiremock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class SabadellAgentWireMockTest {

    private static final String USERNAME = "dummyUsername";
    private static final String PASSWORD = "dummyPassword";

    @Test
    public void testRefresh() throws Exception {

        // Given
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/sabadell/wiremock/resources/wiremockfile_sabadell_log.aap";

        final String wireMockContractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/sabadell/wiremock/resources/agent-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.ES, "es-bancosabadell-password", wireMockServerFilePath)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Field.Key.PASSWORD.getFieldKey(), PASSWORD)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
