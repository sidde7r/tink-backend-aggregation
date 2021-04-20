package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.wiremock;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class CajamarAgentWiremockTest {

    private static final String USERNAME = "dummyUsername";
    private static final String PASSWORD = "dummyPassword";
    private static final String PROVIDER_NAME = "es-cajamar-password";

    @Test
    public void testRefresh() throws Exception {

        // Given
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/cajamar/wiremock/resources/cajamar_wiremock_test.aap";

        final String wireMockContractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/cajamar/wiremock/resources/agent-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        Set<RefreshableItem> refreshableItems =
                new HashSet<>(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        refreshableItems.add(RefreshableItem.IDENTITY_DATA);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.ES)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .withRefreshableItems(refreshableItems)
                        .addCredentialField(Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Key.PASSWORD.getFieldKey(), PASSWORD)
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
