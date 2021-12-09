package se.tink.backend.aggregation.agents.nxgen.lt.openbanking.luminor.wiremock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class LuminorLTAgentWiremockTest {

    final String CONFIG_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/lt/openbanking/luminor/wiremock/resources/configuration.yml";

    @Test
    public void testRefresh() throws Exception {

        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/lt/openbanking/luminor/wiremock/resources/lt-luminor-ob-ais.aap";

        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/lt/openbanking/luminor/wiremock/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIG_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.LT)
                        .withProviderName("lt-luminor-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "dummyCode")
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "user1")
                        .addCredentialField(Field.Key.CORPORATE_ID.getFieldKey(), "user1Id")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        agentWireMockRefreshTest.executeRefresh();

        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
