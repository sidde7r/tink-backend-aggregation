package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.mock.ais;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class CbiGlobeAisMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe/mock/ais/resources/";

    @Test
    // This is a generic test that covers default behaviour of CBI agents.
    // Some child agents have their own mocks, where the flow is not standard, or where it was
    // judged as necessary.
    public void shouldRefreshDataCorrectly() throws Exception {
        // given
        AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(BASE_PATH + "configuration.yml");

        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.IT)
                        .withProviderName("it-credem-oauth2")
                        .withWireMockFilePath(BASE_PATH + "full_auth_and_refresh.aap")
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
