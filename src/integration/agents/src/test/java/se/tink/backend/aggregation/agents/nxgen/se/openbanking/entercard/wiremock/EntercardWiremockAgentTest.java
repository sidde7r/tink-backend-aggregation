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
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.SE, "se-remembermastercard-ob", wireMockServerFilePath)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), MOCKED_SSN)
                        .addCallbackData("code", "dummyCode")
                        .withConfigurationFile(configuration)
                        .addRefreshableItems()
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        agentWireMockRefreshTest.executeRefresh();

        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
