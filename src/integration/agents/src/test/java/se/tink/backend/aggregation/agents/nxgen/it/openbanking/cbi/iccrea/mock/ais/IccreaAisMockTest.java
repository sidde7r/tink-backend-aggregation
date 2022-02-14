package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.iccrea.mock.ais;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.enums.MarketCode;

public class IccreaAisMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/cbi/iccrea/mock/ais/resources/";

    private AgentsServiceConfiguration configuration;

    @Before
    public void setup() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(BASE_PATH + "configuration.yml");
    }

    @Test
    public void shouldCompleteFullManualAuthentication() {
        // given
        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.IT)
                        .withProviderName("it-iccrea-no-08530-oauth2")
                        .withWireMockFilePath(BASE_PATH + "full_auth.aap")
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCredentialField("username", "test_username")
                        .addCredentialField("password", "test_password")
                        .build();

        // when & then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void shouldCompleteAutoAuthWhenConsentInStorageValid() {
        // given
        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.IT)
                        .withProviderName("it-iccrea-no-08530-oauth2")
                        .withWireMockFilePath(BASE_PATH + "auto_auth.aap")
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData("consent-id", "test_consent_id")
                        .build();

        // when & then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }
}
