package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.wiremock;

import io.dropwizard.configuration.ConfigurationException;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class CrosskeyAgentWireMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/crosskey/wiremock/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";
    private static final String WIREMOCK_SERVER_FILEPATH = BASE_PATH + "wiremockfile.aap";
    private static final String WIREMOCK_SERVER_FILEPATH_RESURS_SUPREME =
            BASE_PATH + "wiremockfile_supreme.aap";
    private static final String WIREMOCK_SERVER_FILEPATH_RESURSBANK =
            BASE_PATH + "wiremockfile_resursbank.aap";
    private static final String CONTRACT_FILE_PATH = BASE_PATH + "agent-contract.json";
    private static final String CONTRACT_FILE_PATH_RESURS_SUPREME =
            BASE_PATH + "agent-contract-supreme.json";
    private static final String CONTRACT_FILE_PATH_RESURSBANK =
            BASE_PATH + "agent-contract-resursbank.json";

    private AgentWireMockRefreshTest buildAgentWireMockRefreshTest(
            String providerName, String wiremockFile) throws IOException, ConfigurationException {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        return AgentWireMockRefreshTest.nxBuilder()
                .withMarketCode(MarketCode.SE)
                .withProviderName(providerName)
                .withWireMockFilePath(wiremockFile)
                .withConfigFile(configuration)
                .testFullAuthentication()
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addCallbackData("code", "dummyCode")
                .build();
    }

    @Test
    public void testRefresh() throws Exception {
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildAgentWireMockRefreshTest("se-alandsbanken-ob", WIREMOCK_SERVER_FILEPATH);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);
        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testRefreshResursBankSupremeCard() throws Exception {

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildAgentWireMockRefreshTest(
                        "se-resursbank-ob", WIREMOCK_SERVER_FILEPATH_RESURS_SUPREME);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH_RESURS_SUPREME);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testRefreshResursbank() throws Exception {
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildAgentWireMockRefreshTest(
                        "se-resursbank-ob", WIREMOCK_SERVER_FILEPATH_RESURSBANK);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH_RESURSBANK);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
