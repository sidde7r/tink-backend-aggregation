package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.integration;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.integration.module.IngAgentWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.enums.MarketCode;

public class IngAgentWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/ing/integration/resources/";
    private static final String CONFIG_PATH = RESOURCES_PATH + "configuration.yml";

    @Test
    public void testClientCanAuthenticateManuallyForTheFirstTime() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "ing_manual_first_authorization.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIG_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName("be-ing-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("code", "DUMMY_CODE")
                        .withAgentTestModule(new IngAgentWireMockTestModule())
                        .build();

        // then
        Assertions.assertThatCode(agentWireMockRefreshTest::executeRefresh)
                .doesNotThrowAnyException();
    }
}
