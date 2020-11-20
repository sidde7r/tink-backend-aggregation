package se.tink.backend.aggregation.agents.nxgen.se.business.swedbank.mock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class SwedbankBusinessAgentWireMockTest {
    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/business/swedbank/mock/resources/configuration.yml";

    private AgentWireMockRefreshTest buildAgentWireMockRefreshTest(
            String providerName,
            String wireMockFilePath,
            AgentsServiceConfiguration configuration) {
        return AgentWireMockRefreshTest.builder(MarketCode.SE, providerName, wireMockFilePath)
                .withConfigurationFile(configuration)
                .addCredentialField(Key.CORPORATE_ID.getFieldKey(), "555555-5555")
                .addCredentialField(Key.USERNAME.getFieldKey(), "200012121212")
                .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                .addRefreshableItems(RefreshableItem.CHECKING_TRANSACTIONS)
                .addRefreshableItems(RefreshableItem.SAVING_ACCOUNTS)
                .addRefreshableItems(RefreshableItem.SAVING_TRANSACTIONS)
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build();
    }

    @Test
    public void testSwedbankBaseRefresh() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/business/swedbank/mock/resources/swedbank_business_base.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/business/swedbank/mock/resources/agent-contract-base.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildAgentWireMockRefreshTest(
                        "swedbank-business-bankid", wireMockFilePath, configuration);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testSwedbankRefreshNoPaymentPermissions() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/business/swedbank/mock/resources/swedbank_business_no_payment_permissions.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/business/swedbank/mock/resources/agent-contract-edge-cases.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildAgentWireMockRefreshTest(
                        "swedbank-business-bankid", wireMockFilePath, configuration);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testSavingsbankRefreshMultipleBankProfiles() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/business/swedbank/mock/resources/savingsbank-business-multiple-banks.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/business/swedbank/mock/resources/agent-contract-edge-cases.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildAgentWireMockRefreshTest(
                        "savingsbank-business-bankid", wireMockFilePath, configuration);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
