package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.integration;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.integration.module.CeWireMockTestModule;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class CeWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/caisseepargne/integration/resources/";

    @Test
    public void test() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "ce_mock_log.aap";
        final String contractFilePath = RESOURCES_PATH + "agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.FR,
                                "fr-caisseepargneiledefrance-password",
                                wireMockFilePath)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "DUMMY_USER")
                        .addCredentialField(Field.Key.PASSWORD.getFieldKey(), "DUMMY_PASSWORD")
                        .addCallbackData(Field.Key.OTP_INPUT.getFieldKey(), "DUMMY_OTP_CODE")
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withAgentModule(new CeWireMockTestModule())
                        .withHttpDebugTrace()
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
