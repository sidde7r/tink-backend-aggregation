package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank.wiremock.account;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class DeutscheBankAccountWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/deutschebank/wiremock/account/resources/";

    @Test
    public void shouldRunFullAuthRefreshSuccessfully() throws Exception {

        // given
        final String configurationFilePath = RESOURCES_PATH + "configuration.yml";
        final String wireMockFilePath = RESOURCES_PATH + "refresh-with-pagination.aap";
        final String contractFilePath = RESOURCES_PATH + "contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationFilePath);
        final Set<RefreshableItem> refreshableItems =
                new HashSet<>(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        refreshableItems.remove(RefreshableItem.TRANSFER_DESTINATIONS);
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.DE)
                        .withProviderName("de-deutschebank-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(refreshableItems.toArray(new RefreshableItem[0]))
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
