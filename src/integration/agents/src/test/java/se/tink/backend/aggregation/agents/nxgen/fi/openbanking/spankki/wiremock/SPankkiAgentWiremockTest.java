package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki.wiremock.module.SPankkiAgentWiremockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class SPankkiAgentWiremockTest {

    private static final String PROVIDER_NAME = "fi-spankki-ob";

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/spankki/wiremock/resources/configuration.yml";

    private static final String WIREMOCK_SERVER_FILEPATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/spankki/wiremock/resources/spankki_ob_wiremock.aap";

    private static final String CONTRACT_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/spankki/wiremock/resources/agent-contract.json";

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
                        .withAgentTestModule(new SPankkiAgentWiremockTestModule())
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
