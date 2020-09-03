package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.integration;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.integration.module.BpRiversParisWireMockTestModule;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class BpRivesParisWireMockTest {

    private static final String BANK_SHORT_ID = "002";
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/banquepopulaire/integration/resources/";

    @Test
    public void test() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "bp_rivesparis_mock_log.aap";
        final String contractFilePath = RESOURCES_PATH + "agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.FR,
                                "fr-banquepopulairerivesdeparis-password",
                                wireMockFilePath)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "DUMMY_USER")
                        .addCredentialField(Field.Key.PASSWORD.getFieldKey(), "DUMMY_PASSWORD")
                        .addCredentialPayload(BANK_SHORT_ID)
                        .addCallbackData(Field.Key.OTP_INPUT.getFieldKey(), "DUMMY_OTP_CODE")
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withAgentModule(new BpRiversParisWireMockTestModule())
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
