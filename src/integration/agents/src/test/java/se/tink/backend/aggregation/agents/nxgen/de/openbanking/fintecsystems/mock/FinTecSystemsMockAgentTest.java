package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems.mock;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class FinTecSystemsMockAgentTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/mock/resources/";

    private static final String CONFIGURATION_FILE = BASE_PATH + "configuration.yml";
    private static final String CONTRACT_FILE = BASE_PATH + "contract.json";
    private static final String AAP_FILE = BASE_PATH + "accountReport.aap";

    private AgentsServiceConfiguration configuration;

    @Before
    public void setup() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);
    }

    @Test
    public void shouldFetchAccountReportCorrectly() throws Exception {
        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.DE)
                        .withProviderName("de-test-fintecsystems")
                        .withWireMockFilePath(AAP_FILE)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addPersistentStorageData("TRANSACTION_ID", "1234567890")
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser().parseContractOnBasisOfFile(CONTRACT_FILE);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
