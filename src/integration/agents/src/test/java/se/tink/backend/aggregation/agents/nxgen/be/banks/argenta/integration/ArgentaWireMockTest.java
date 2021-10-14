package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.integration;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

@RunWith(JUnitParamsRunner.class)
public class ArgentaWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/argenta/integration/resources/";

    private static final String AGENT_CONTRACT_FILE_PATH = RESOURCES_PATH + "agent-contract.json";

    @Test
    public void testFullRefresh() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "argenta_mock_log.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName("be-argenta-cardreader")
                        .withWireMockFilePath(wireMockFilePath)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("logininput", "12345678")
                        .addCredentialField("username", "67031234567890001")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(AGENT_CONTRACT_FILE_PATH);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testFullRefreshWithSmsCodeValidation() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "argenta_sms_code_mock_log.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName("be-argenta-cardreader")
                        .withWireMockFilePath(wireMockFilePath)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("logininput", "12345678")
                        .addCallbackData("otpinput", "123456")
                        .addCredentialField("username", "67031234567890001")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(AGENT_CONTRACT_FILE_PATH);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    @Parameters({"argenta_auto_auth_mock_log.aap", "argenta_session_valid_mock_log.aap"})
    public void testAutoRefresh(String wireMockFile) throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + wireMockFile;

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName("be-argenta-cardreader")
                        .withWireMockFilePath(wireMockFilePath)
                        .withoutConfigFile()
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addPersistentStorageData(
                                "DEVICE_ID", "00000000-0000-4000-0000-000000000000")
                        .addPersistentStorageData("IS_NEW_CREDENTIAL", "true")
                        .addPersistentStorageData("UAK", "ABC123XYZ")
                        .addSessionStorageData("Authorization", "some_token")
                        .addCredentialField("username", "67031234567890001")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(AGENT_CONTRACT_FILE_PATH);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
