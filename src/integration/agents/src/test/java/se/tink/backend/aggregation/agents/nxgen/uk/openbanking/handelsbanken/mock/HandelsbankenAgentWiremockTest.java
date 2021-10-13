package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class HandelsbankenAgentWiremockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/handelsbanken/mock/resources/";
    static final String configFilePath = RESOURCES_PATH + "configuration.yml";
    private static final String PROVIDER_NAME = "uk-handelsbanken-ob";

    @Test
    public void manualRefreshAll() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "manual-refresh-all.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "manual-refresh-all-contract.json";

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

    @Test
    public void shouldThrowAccountRefreshException() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "account-refresh-exception-test.aap";

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
        Throwable thrown = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // Then
        assertThat(thrown).isExactlyInstanceOf(AccountRefreshException.class);
    }
}
