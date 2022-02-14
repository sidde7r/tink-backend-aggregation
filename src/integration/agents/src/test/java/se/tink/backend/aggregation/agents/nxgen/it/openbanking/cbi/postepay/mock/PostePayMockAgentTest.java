package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.postepay.mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static se.tink.libraries.enums.MarketCode.IT;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;

@Ignore
public class PostePayMockAgentTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/cbi/postepay/mock/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";

    @Test
    public void testFullFlow() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "it-postepay.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(IT)
                        .withProviderName("it-postepay-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("test", "test")
                        .enableHttpDebugTrace()
                        .build();

        // when / then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }
}
