package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

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

    @Test
    public void testRefresh() throws Exception {

        // Given
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ibercaja/resources/ibercaja-traffic.aap";
        final String wireMockContractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ibercaja/resources/contract-file.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.ES)
                        .withProviderName("es-ibercaja-password")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withoutConfigFile()
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.SAVING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.CREDITCARD_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.CREDITCARD_TRANSACTIONS)
                        .addRefreshableItems(RefreshableItem.LOAN_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.LOAN_TRANSACTIONS)
                        .addRefreshableItems(RefreshableItem.INVESTMENT_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.INVESTMENT_TRANSACTIONS)
                        .addRefreshableItems(RefreshableItem.EINVOICES)
                        .addRefreshableItems(RefreshableItem.TRANSFER_DESTINATIONS)
                        .addRefreshableItems(RefreshableItem.LIST_BENEFICIARIES)
                        .addCredentialField(Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Key.PASSWORD.getFieldKey(), PASSWORD)
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
