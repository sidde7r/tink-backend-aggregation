package se.tink.backend.aggregation.agents.nxgen.de.openbanking.degussabank.mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static se.tink.libraries.enums.MarketCode.DE;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;

public class DegussaMockAgentTest {
    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/degussabank/mock/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";
    private static String CONTRACT_FILE_PATH = BASE_PATH + "agent-contract.json";

    @Test
    public void testAuthWithoutSelection() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "authWithoutSelection.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-degussabank-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("smsTan", "123456")
                        .addCredentialField("username", "test_username")
                        .addCredentialField("password", "test_password")
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);
        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
