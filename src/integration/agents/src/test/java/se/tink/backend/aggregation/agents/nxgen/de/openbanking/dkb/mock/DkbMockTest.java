package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static se.tink.libraries.enums.MarketCode.DE;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;

public class DkbMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/dkb/mock/resources/";

    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";

    @Test
    public void testFullFlow() throws Exception {
        // given
        final String mockFilePath = BASE_PATH + "dkb_manual_aggregation.aap";
        final String contractFilePath = BASE_PATH + "dkb_manual_aggregation.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-dkb-ob")
                        .withWireMockFilePath(mockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("pushTan", "123456")
                        .addCredentialField("username", "username")
                        .addCredentialField("password", "password")
                        .enableHttpDebugTrace()
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
