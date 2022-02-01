package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.bpm.mock.ais;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class BpmAisMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/cbi/bpm/mock/ais/resources/";

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
                        .withProviderName("it-bpm-oauth2")
                        .withWireMockFilePath(BASE_PATH + "full_auth.aap")
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
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
                        .withProviderName("it-bpm-oauth2")
                        .withWireMockFilePath(BASE_PATH + "auto_auth.aap")
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData("consent-id", "test_consent_id")
                        .build();

        // when & then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void shouldRefreshDataCorrectly() throws Exception {
        // given
        String[] aapFiles = {BASE_PATH + "full_auth.aap", BASE_PATH + "refresh.aap"};

        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.IT)
                        .withProviderName("it-bpm-oauth2")
                        .withWireMockFilePaths(Arrays.stream(aapFiles).collect(Collectors.toSet()))
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .withRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ALL)
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(BASE_PATH + "contract.json");

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
