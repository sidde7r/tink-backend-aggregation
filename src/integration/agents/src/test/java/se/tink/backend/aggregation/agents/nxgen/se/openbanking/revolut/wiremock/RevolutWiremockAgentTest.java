package se.tink.backend.aggregation.agents.nxgen.se.openbanking.revolut.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class RevolutWiremockAgentTest {

    @Test
    public void testRefresh() throws Exception {

        // given
        final String CONFIGURATION_PATH =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/revolut/wiremock/resources/configuration.yml";

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/revolut/wiremock/resources/revolut_wiremock.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/revolut/wiremock/resources/revolut-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.SE)
                        .withProviderName("se-revolut-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
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
