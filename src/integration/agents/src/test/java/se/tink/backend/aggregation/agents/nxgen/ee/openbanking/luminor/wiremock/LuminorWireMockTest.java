package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.luminor.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class LuminorWireMockTest {

    final String CONFIG_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/ee/openbanking/luminor/wiremock/resources/configuration.yml";

    @Test
    public void testRefresh() throws Exception {

        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/ee/openbanking/luminor/wiremock/resources/ee-luminor-ob-ais.aap";

        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/ee/openbanking/luminor/wiremock/resources/Luminor_contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIG_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.EE)
                        .withProviderName("ee-luminor-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "dummyCode")
                        .addCredentialField("username", "user1")
                        .addCredentialField("psu-corporate-id", "user1Id")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        agentWireMockRefreshTest.executeRefresh();

        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
