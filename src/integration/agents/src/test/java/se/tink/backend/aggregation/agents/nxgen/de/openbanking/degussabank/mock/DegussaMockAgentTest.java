package se.tink.backend.aggregation.agents.nxgen.de.openbanking.degussabank.mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static se.tink.libraries.enums.MarketCode.DE;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest.BuildStep;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;

public class DegussaMockAgentTest {
    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/degussabank/mock/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";
    private static String CONTRACT_FILE_PATH = BASE_PATH + "agent-contract.json";
    private static AgentsServiceConfiguration configuration;

    @Before
    public void init() throws Exception {
        this.configuration = AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    @Test
    public void testAuthWithoutSelection() {
        // given
        final String wireMockFilePath = BASE_PATH + "authWithoutSelection.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                prepareAgentWiremockTestBuilder(wireMockFilePath)
                        .addCallbackData("smsTan", "123456")
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);
        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testAuthWhenOnlyDecoupledAvailable() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "authWithOnlyPush.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                prepareAgentWiremockTestBuilder(wireMockFilePath).build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);
        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    private BuildStep prepareAgentWiremockTestBuilder(String wiremockPath) {
        return AgentWireMockRefreshTest.nxBuilder()
                .withMarketCode(DE)
                .withProviderName("de-degussabank-ob")
                .withWireMockFilePath(wiremockPath)
                .withConfigFile(configuration)
                .testFullAuthentication()
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addCredentialField("username", "test_username")
                .addCredentialField("password", "test_password")
                .enableHttpDebugTrace()
                .enableDataDumpForContractFile();
    }
}
