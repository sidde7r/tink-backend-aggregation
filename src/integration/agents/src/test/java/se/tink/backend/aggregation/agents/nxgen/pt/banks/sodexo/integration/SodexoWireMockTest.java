package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.integration;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class SodexoWireMockTest {
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/banks/sodexo/integration/resources/";
    private static final String CONTRACT_FILE_PATH = RESOURCE_PATH + "agent-contract.json";

    @Test
    public void testFullRefresh() throws Exception {
        // given
        final String filePath = RESOURCE_PATH + "sodexo_full_refresh.aap";
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.PT)
                        .withProviderName("pt-sodexo-password")
                        .withWireMockFilePath(filePath)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCredentialField("username", "123456789")
                        .addCredentialField("password", "coco_jumbo_i_do_przodu")
                        .enableDataDumpForContractFile()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testAutoRefresh() throws Exception {
        // given
        final String filePath = RESOURCE_PATH + "sodexo_auto_refresh.aap";
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.PT)
                        .withProviderName("pt-sodexo-password")
                        .withWireMockFilePath(filePath)
                        .withoutConfigFile()
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCredentialField("username", "123456789")
                        .addCredentialField("password", "coco_jumbo_i_do_przodu")
                        .addPersistentStorageData("PIN", "0000")
                        .addPersistentStorageData("USER_TOKEN", "token.test.test2")
                        .addPersistentStorageData("NAME", "TestName")
                        .addPersistentStorageData("SURNAME", "TestSurname")
                        .addPersistentStorageData("CARD_NUMBER", "1234")
                        .enableDataDumpForContractFile()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
