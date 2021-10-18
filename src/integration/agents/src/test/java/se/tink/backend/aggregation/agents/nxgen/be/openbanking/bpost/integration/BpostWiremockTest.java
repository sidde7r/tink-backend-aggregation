package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.integration;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest.FullOrAutoAuthenticationStep;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class BpostWiremockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/bpost/integration/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";
    private static final String PROVIDER_NAME = "be-bpost-ob";

    private AgentsServiceConfiguration configuration;

    @Before
    public void init() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    @Test
    public void testAutoRefresh() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "bpost_wiremockfile.aap";
        final String agentContractFilePath = BASE_PATH + "agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                prepareTestWithConfiguration(wireMockFilePath)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addPersistentStorageData("consent_id", "DUMMY_CONSENT_ID")
                        .addPersistentStorageData("sca_approach", "DECOUPLED")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(agentContractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testAuthentication() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "bpost_wiremockfile_auth.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                prepareTestWithConfiguration(wireMockFilePath)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("code", "DUMMY_CODE")
                        .build();

        // then
        Assertions.assertThatCode(agentWireMockRefreshTest::executeRefresh)
                .doesNotThrowAnyException();
    }

    private FullOrAutoAuthenticationStep prepareTestWithConfiguration(String wireMockFilePath) {
        return AgentWireMockRefreshTest.nxBuilder()
                .withMarketCode(MarketCode.BE)
                .withProviderName(PROVIDER_NAME)
                .withWireMockFilePath(wireMockFilePath)
                .withConfigFile(configuration);
    }
}
