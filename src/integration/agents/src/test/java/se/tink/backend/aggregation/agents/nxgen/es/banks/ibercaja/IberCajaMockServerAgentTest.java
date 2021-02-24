package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class IberCajaMockServerAgentTest {

    private static final String USERNAME = "dummyUsername";
    private static final String PASSWORD = "dummyPassword";

    /*
    Transactions excluded because pagination is using dynamic dates, while .aap has static content
     */
    @Test
    public void testRefreshWithoutTransactions() throws Exception {

        // Given
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ibercaja/resources/ibercaja-traffic.aap";
        final String wireMockContractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ibercaja/resources/contract-file.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        Set<RefreshableItem> refreshableItems =
                new HashSet<>(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        refreshableItems.add(RefreshableItem.IDENTITY_DATA);
        refreshableItems.remove(RefreshableItem.CHECKING_TRANSACTIONS);
        refreshableItems.remove(RefreshableItem.SAVING_TRANSACTIONS);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.ES)
                        .withProviderName("es-ibercaja-password")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withoutConfigFile()
                        .withRefreshableItems(refreshableItems)
                        .addCredentialField(Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Key.PASSWORD.getFieldKey(), PASSWORD)
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
