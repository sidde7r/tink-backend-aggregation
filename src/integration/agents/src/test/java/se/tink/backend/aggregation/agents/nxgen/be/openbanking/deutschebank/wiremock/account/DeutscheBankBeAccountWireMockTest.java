package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.wiremock.account;

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

public class DeutscheBankBeAccountWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/deutschebank/wiremock/account/resources/";
    private static final String CONFIG_FILE_PATH = RESOURCES_PATH + "configuration.yml";
    private static final String CONTRACT_FILE_PATH = RESOURCES_PATH + "contract.json";

    @Test
    public void shouldRunFullAuthRefreshSuccessfully() throws Exception {

        // given
        final String wireMockFilePath = RESOURCES_PATH + "refresh-with-pagination.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIG_FILE_PATH);
        final Set<RefreshableItem> refreshableItems =
                new HashSet<>(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        refreshableItems.remove(RefreshableItem.TRANSFER_DESTINATIONS);
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName("be-deutschebank-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(refreshableItems.toArray(new RefreshableItem[0]))
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
    public void shouldRunAutoAuthRefreshSuccessfully() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "refresh-with-auto-authentication.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIG_FILE_PATH);
        final Set<RefreshableItem> refreshableItems =
                new HashSet<>(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        refreshableItems.remove(RefreshableItem.TRANSFER_DESTINATIONS);
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName("be-deutschebank-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(refreshableItems.toArray(new RefreshableItem[0]))
                        .addPersistentStorageData("Consent-ID", "consentId")
                        .enableDataDumpForContractFile()
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
