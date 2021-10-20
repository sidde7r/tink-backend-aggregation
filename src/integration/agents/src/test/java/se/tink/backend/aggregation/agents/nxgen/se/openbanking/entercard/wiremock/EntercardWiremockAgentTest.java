package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.wiremock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class EntercardWiremockAgentTest {
    private static final String MOCKED_SSN = "199909121213";
    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/entercard/wiremock/resources/configuration.yml";

    @Test
    public void testRemembermastercardRefresh() throws Exception {
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/entercard/wiremock/resources/remember_wiremock.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/entercard/wiremock/resources/remember_agent_contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildWiremockRefreshTest(wireMockServerFilePath, "se-remembermastercard-ob");
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        agentWireMockRefreshTest.executeRefresh();
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testCoopSeRefresh() throws Exception {
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/entercard/wiremock/resources/coop_wiremock.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/entercard/wiremock/resources/coop_agent_contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildWiremockRefreshTest(wireMockServerFilePath, "se-coop-ob");

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        agentWireMockRefreshTest.executeRefresh();

        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    private AgentWireMockRefreshTest buildWiremockRefreshTest(
            String wireMockServerFilePath, String providerName) throws Exception {

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        return AgentWireMockRefreshTest.nxBuilder()
                .withMarketCode(MarketCode.SE)
                .withProviderName(providerName)
                .withWireMockFilePath(wireMockServerFilePath)
                .withConfigFile(configuration)
                .testFullAuthentication()
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addCredentialField(Field.Key.USERNAME.getFieldKey(), MOCKED_SSN)
                .addCallbackData("code", "dummyCode")
                .build();
    }
}
