package se.tink.backend.aggregation.agents.nxgen.it.openbanking.fabric.sellapersonalcredit.wiremock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static se.tink.libraries.enums.MarketCode.IT;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public class SellaPersonalCreditWiremockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/fabric/sellapersonalcredit/wiremock/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";
    private static final String WIREMOCK_FILE_PATH = BASE_PATH + "authentication_requests.aap";

    @Test
    public void testAuthWithoutSelection() throws Exception {
        // given

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(IT)
                        .withProviderName("it-sellapersonalcredit-ob")
                        .withWireMockFilePath(WIREMOCK_FILE_PATH)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("smsOtpField", "123456")
                        .addCredentialField("username", "test_username")
                        .addCredentialField("password", "test_password")
                        .enableHttpDebugTrace()
                        .build();

        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void testAuthWithSelection() throws Exception {
        // given
        String wiremockMultipleScaFilePath = BASE_PATH + "authentication_requests_multiple_sca.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(IT)
                        .withProviderName("it-sellapersonalcredit-ob")
                        .withWireMockFilePath(wiremockMultipleScaFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("smsOtpField", "123456")
                        .addCredentialField("username", "test_username")
                        .addCredentialField("password", "test_password")
                        .enableHttpDebugTrace()
                        .build();

        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }
}
