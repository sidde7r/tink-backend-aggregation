package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.mock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class HandelsbankenCorporateAgentWiremockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/handelsbanken/mock/resources/";
    static final String configFilePath = RESOURCES_PATH + "configuration.yml";
    private static final String PROVIDER_NAME = "uk-handelsbanken-corporate-ob";

    @Test
    public void manualRefreshAll() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "corporate-manual-refresh-all.aap";
        final String wireMockContractFilePath =
                RESOURCES_PATH + "corporate-manual-refresh-all-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(AgentsServiceConfigurationReader.read(configFilePath))
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "DUMMY_ACCESS_TOKEN2")
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
