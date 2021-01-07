package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class IngMockServerAgentTest {
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ing/v195/resources/";

    private static final String PROVIDER_NAME = "es-ing-password";
    private static final String USERNAME = "1234567X";
    private static final String PASSWORD = "123456";
    private static final String DATE_OF_BIRTH = "12122012";
    private static final String OTP_INPUT = "13371337";

    @Test
    public void testLoginWithOtp() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "ing-login-with-otp.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "login-agent-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.ES, PROVIDER_NAME, wireMockServerFilePath)
                        .addCredentialField(Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Key.PASSWORD.getFieldKey(), PASSWORD)
                        .addCredentialField(Key.DATE_OF_BIRTH.getFieldKey(), DATE_OF_BIRTH)
                        .addCallbackData(Field.Key.OTP_INPUT.getFieldKey(), OTP_INPUT)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withRequestFlagCreate(true)
                        .withConfigurationFile(
                                AgentsServiceConfigurationReader.read(
                                        RESOURCES_PATH + "configuration.yml"))
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testLoginWithoutSca() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "ing-login.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "login-agent-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.ES, PROVIDER_NAME, wireMockServerFilePath)
                        .addCredentialField(Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Key.PASSWORD.getFieldKey(), PASSWORD)
                        .addCredentialField(Key.DATE_OF_BIRTH.getFieldKey(), DATE_OF_BIRTH)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withConfigurationFile(
                                AgentsServiceConfigurationReader.read(
                                        RESOURCES_PATH + "configuration.yml"))
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testLoginWithPush() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "ing-login-with-push.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "login-agent-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.ES, PROVIDER_NAME, wireMockServerFilePath)
                        .addCredentialField(Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Key.PASSWORD.getFieldKey(), PASSWORD)
                        .addCredentialField(Key.DATE_OF_BIRTH.getFieldKey(), DATE_OF_BIRTH)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withRequestFlagCreate(true)
                        .withConfigurationFile(
                                AgentsServiceConfigurationReader.read(
                                        RESOURCES_PATH + "configuration.yml"))
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testFetchAccounts() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "fetch-accounts.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "fetch-accounts-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.ES, PROVIDER_NAME, wireMockServerFilePath)
                        .addCredentialField(Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Key.PASSWORD.getFieldKey(), PASSWORD)
                        .addCredentialField(Key.DATE_OF_BIRTH.getFieldKey(), DATE_OF_BIRTH)
                        .addCallbackData(Field.Key.OTP_INPUT.getFieldKey(), OTP_INPUT)
                        .addRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ACCOUNTS)
                        .withConfigurationFile(
                                AgentsServiceConfigurationReader.read(
                                        RESOURCES_PATH + "configuration.yml"))
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
