package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ing.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class IngWiremockTest {

    @Test
    public void testRefresh() throws Exception {
        final String configurationPath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/ing/wiremock/resources/configuration.yml";
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/ing/wiremock/resources/it-ing-ob-ais.aap";

        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/ing/wiremock/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.IT)
                        .withProviderName("it-ing-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "dummyCode")
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
