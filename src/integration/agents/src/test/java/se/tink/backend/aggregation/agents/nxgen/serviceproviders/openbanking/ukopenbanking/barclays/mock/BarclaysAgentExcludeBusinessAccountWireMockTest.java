package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.mock;

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

public class BarclaysAgentExcludeBusinessAccountWireMockTest {

    private static final String CONFIGURATION_PATH = "data/agents/uk/barclays/configuration.yml";

    @Test
    public void shouldExcludeBusinessAccountsWhileFetchingFromProviderForPersonalAccounts()
            throws Exception {

        // given
        final String wireMockFilePath =
                "data/agents/uk/barclays/mock_log_exclude_business_account.aap";
        final String contractFilePath =
                "data/agents/uk/barclays/agent-contract_exclude_business_account.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final Set<RefreshableItem> refreshableItems =
                new HashSet<>(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        refreshableItems.remove(RefreshableItem.TRANSFER_DESTINATIONS);
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.UK, "uk-barclays-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
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
