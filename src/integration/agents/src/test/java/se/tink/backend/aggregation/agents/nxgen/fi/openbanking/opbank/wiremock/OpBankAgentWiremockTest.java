package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.wiremock.module.OpBankAgentWiremockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class OpBankAgentWiremockTest {

    private static final String PROVIDER_NAME = "fi-opbank-ob";

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/opbank/wiremock/resources/configuration.yml";

    private static final String WIREMOCK_SERVER_FILEPATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/opbank/wiremock/resources/op_bank_ob_wiremock.aap";

    private static final String CONTRACT_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/opbank/wiremock/resources/agent-contract.json";

    @Test
    public void testRefresh() throws Exception {

        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.FI)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(WIREMOCK_SERVER_FILEPATH)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.CHECKING_TRANSACTIONS)
                        .addRefreshableItems(RefreshableItem.CREDITCARD_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.CREDITCARD_TRANSACTIONS)
                        .addCallbackData("code", "dummyCode")
                        .withAgentTestModule(new OpBankAgentWiremockTestModule())
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
