package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static se.tink.libraries.enums.MarketCode.DE;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public class UnicreditMockAgentTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/unicredit/mock/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";

    @Test
    public void testFullAuth() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "full_auth.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-unicredit-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("smsTan", "123456")
                        .addCredentialField("username", "test_username")
                        .addCredentialField("password", "test_password")
                        .enableHttpDebugTrace()
                        .build();

        // when / then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }
}
