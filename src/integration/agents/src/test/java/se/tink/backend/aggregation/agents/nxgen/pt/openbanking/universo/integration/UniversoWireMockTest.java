package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.integration;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.integration.module.UniversoWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class UniversoWireMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/openbanking/universo/integration/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";

    private AgentsServiceConfiguration configuration;

    @Before
    public void init() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    @Test
    public void testSuccessful() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "mock_log.aap";
        final String contractFilePath = BASE_PATH + "agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.PT)
                        .withProviderName("pt-universo-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "code")
                        .withAgentTestModule(new UniversoWireMockTestModule())
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
