package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.starling.wiremock;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class StarlingAgentWiremockTest {

    private static final String CONFIGURATION_PATH = "data/agents/uk/starling/configuration.yml";
    private static final String OAUTH_TOKEN =
            "{\"expires_in\" : 999999999, \"issuedAt\": 1598516000, \"token_type\":\"bearer\",  \"access_token\":\"DUMMY_OAUTH2_TOKEN\"}";

    private static final String OAUTH_TOKEN_EXPIRED =
            "{\"expires_in\" : 0, \"issuedAt\": 1598516000, \"token_type\":\"bearer\", \"access_token\":\"DUMMY_OAUTH2_TOKEN_EXPIRED\", \"refresh_token\":\"DUMMY_REFRESH_TOKEN\"}";

    @Test
    public void testRefresh() throws Exception {
        final Set<RefreshableItem> refreshableItems =
                new HashSet<>(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        refreshableItems.remove(RefreshableItem.TRANSFER_DESTINATIONS);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.UK,
                                "uk-starling-oauth2",
                                "data/agents/uk/starling/mock_log.aap")
                        .withConfigurationFile(
                                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .addRefreshableItems(refreshableItems.toArray(new RefreshableItem[0]))
                        .dumpContentForContractFile()
                        .addPersistentStorageData("OAUTH2_TOKEN", OAUTH_TOKEN)
                        .withHttpDebugTrace()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile("data/agents/uk/starling/agent-contract.json");
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void businessAccountsAreNotReturned() throws Exception {
        final Set<RefreshableItem> refreshableItems =
                new HashSet<>(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        refreshableItems.remove(RefreshableItem.TRANSFER_DESTINATIONS);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.UK,
                                "uk-starling-oauth2",
                                "data/agents/uk/starling/mock_log_business.aap")
                        .withConfigurationFile(
                                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .addRefreshableItems(refreshableItems.toArray(new RefreshableItem[0]))
                        .dumpContentForContractFile()
                        .addPersistentStorageData("OAUTH2_TOKEN", OAUTH_TOKEN)
                        .withHttpDebugTrace()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(
                                "data/agents/uk/starling/agent-contract-business.json");
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testInvalidGrantHandling() throws Exception {
        final Set<RefreshableItem> refreshableItems =
                new HashSet<>(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        refreshableItems.remove(RefreshableItem.TRANSFER_DESTINATIONS);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.UK,
                                "uk-starling-oauth2",
                                "data/agents/uk/starling/mock_invalid_grant.aap")
                        .withConfigurationFile(
                                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .addRefreshableItems(refreshableItems.toArray(new RefreshableItem[0]))
                        .dumpContentForContractFile()
                        .addCallbackData("code", "DUMMY_CODE")
                        .addPersistentStorageData("OAUTH2_TOKEN", OAUTH_TOKEN_EXPIRED)
                        .withHttpDebugTrace()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile("data/agents/uk/starling/agent-contract.json");
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
