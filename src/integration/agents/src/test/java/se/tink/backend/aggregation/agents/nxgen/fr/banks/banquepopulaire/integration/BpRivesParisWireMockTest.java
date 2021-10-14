package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.integration;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.integration.module.BpRiversParisWireMockTestModule;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class BpRivesParisWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/banquepopulaire/integration/resources/";

    @Test
    public void shouldRefresh() throws Exception {

        // given
        String wireMockFilePath = RESOURCES_PATH + "bp_rivesparis_mock_log.aap";
        String contractFilePath = RESOURCES_PATH + "agent-contract.json";

        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.FR)
                        .withProviderName("fr-banquepopulairerivesdeparis-password")
                        .withWireMockFilePath(wireMockFilePath)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "DUMMY_USER")
                        .addCredentialField(Field.Key.PASSWORD.getFieldKey(), "DUMMY_PASSWORD")
                        .addCallbackData(Field.Key.OTP_INPUT.getFieldKey(), "DUMMY_OTP_CODE")
                        .withAgentTestModule(new BpRiversParisWireMockTestModule())
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void shouldFailOnRefreshBecauseOfConnectionReset() {

        // given
        String wireMockFilePath = RESOURCES_PATH + "bp_rivesparis_connection_reset_mock_log.aap";

        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.FR)
                        .withProviderName("fr-banquepopulairerivesdeparis-password")
                        .withWireMockFilePath(wireMockFilePath)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withAgentTestModule(new BpRiversParisWireMockTestModule())
                        .build();

        // expect
        assertThatExceptionOfType(BankServiceException.class)
                .isThrownBy(agentWireMockRefreshTest::executeRefresh)
                .havingRootCause()
                .withMessageContaining("Connection reset");
    }
}
