package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.wiremock;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.wiremock.module.SamlinkAgentWiremockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class SamlinkAgentWiremockTest {

    private static final String SAASTOPANKKI_PROVIDER_NAME = "fi-saastopankki-ob";

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/samlink/wiremock/resources/configuration.yml";

    private static final String WIREMOCK_SERVER_FILEPATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/samlink/wiremock/resources/saastopankki_ob_wiremock.aap";

    private static final String CONTRACT_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/samlink/wiremock/resources/saastopankki-agent-contract.json";

    private static final String WIREMOCK_SERVER_FILEPATH_NO_API =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/samlink/wiremock/resources/saastopankki_ob_wiremock_no_api.aap";

    @Test
    public void testRefresh() throws Exception {

        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.FI)
                        .withProviderName(SAASTOPANKKI_PROVIDER_NAME)
                        .withWireMockFilePath(WIREMOCK_SERVER_FILEPATH)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "dummyCode")
                        .withAgentTestModule(new SamlinkAgentWiremockTestModule())
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testRefreshNoApi() throws Exception {

        // given
        final String message =
                "com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'Content': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n"
                        + " at [Source: (ByteArrayInputStream); line: 1, column: 9]";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.FI)
                        .withProviderName(SAASTOPANKKI_PROVIDER_NAME)
                        .withWireMockFilePath(WIREMOCK_SERVER_FILEPATH_NO_API)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "dummyCode")
                        .withAgentTestModule(new SamlinkAgentWiremockTestModule())
                        .build();

        Throwable exception = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        assertThat(exception).hasCauseInstanceOf(RuntimeException.class).hasMessage(message);
    }
}
