package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static se.tink.libraries.enums.MarketCode.DE;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public class SparkassenMockAgentTest {
    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/sparkassen/mock/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";

    @Test
    public void testAuthWithoutSelection() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "authWithoutSelection.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-sparkassestadmünchen-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("pushTan", "123456")
                        .addCredentialField("username", "test_username")
                        .addCredentialField("password", "test_password")
                        .enableHttpDebugTrace()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void testAuthWithSelection() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "authWithSelection.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-sparkassestadmünchen-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("pushTan", "123456")
                        .addCallbackData("selectAuthMethodField", "1")
                        .addCredentialField("username", "test_username")
                        .addCredentialField("password", "test_password")
                        .enableHttpDebugTrace()
                        .build();

        // when / then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void testAuthWithSelectionDecoupled() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "authWithSelectionDecoupled.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-sparkassestadmünchen-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("selectAuthMethodField", "2")
                        .addCredentialField("username", "test_username")
                        .addCredentialField("password", "test_password")
                        .enableHttpDebugTrace()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void testAuthWithSelectionExempted() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "authWithSelectionExempted.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-sparkassestadmünchen-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("selectAuthMethodField", "1")
                        .addCredentialField("username", "test_username")
                        .addCredentialField("password", "test_password")
                        .enableHttpDebugTrace()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }
}
