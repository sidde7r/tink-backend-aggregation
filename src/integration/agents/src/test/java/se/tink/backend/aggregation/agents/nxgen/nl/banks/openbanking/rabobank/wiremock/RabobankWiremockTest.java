package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.wiremock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey.CONSENT_ID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN;

import lombok.SneakyThrows;
import org.junit.AfterClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.wiremock.module.RabobankWiremockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class RabobankWiremockTest {

    private static final WireMockTestServer WIRE_MOCK_TEST_SERVER = new WireMockTestServer();

    private final AgentsServiceConfiguration configuration = readConfiguration();

    private final OAuth2Token oauth2Token =
            OAuth2Token.create("bearer", "test_access_token", "test_refresh_token", 120);

    @AfterClass
    public static void cleanUpClass() {
        WIRE_MOCK_TEST_SERVER.shutdown();
    }

    @Test
    public void shouldRefresh() throws Exception {
        // given
        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-rabobank-ob")
                        .withWireMockServer(WIRE_MOCK_TEST_SERVER)
                        .withWireMockFilePath(path("nl-rabobank-ob-ais.aap"))
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "dummyCode")
                        .withAgentTestModule(new RabobankWiremockTestModule())
                        .build();

        // and
        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(path("agent-contract.json"));

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void shouldAutoRefresh() throws Exception {
        // given
        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-rabobank-ob")
                        .withWireMockServer(WIRE_MOCK_TEST_SERVER)
                        .withWireMockFilePath(path("nl-rabobank-ob-ais.aap"))
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(CONSENT_ID, "fffff")
                        .addPersistentStorageData(OAUTH_2_TOKEN, oauth2Token)
                        .withAgentTestModule(new RabobankWiremockTestModule())
                        .build();

        // and
        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(path("agent-contract-no-user.json"));

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void shouldSuccessfullyRetryOnRateLimitExceeded() throws Exception {
        // given
        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-rabobank-ob")
                        .withWireMockServer(WIRE_MOCK_TEST_SERVER)
                        .withWireMockFilePath(path("nl-rabobank-ob-ais-rate-limit-exceeded.aap"))
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(CONSENT_ID, "fffff")
                        .addPersistentStorageData(OAUTH_2_TOKEN, oauth2Token)
                        .withAgentTestModule(new RabobankWiremockTestModule())
                        .build();

        // and
        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(path("agent-contract-one-account.json"));

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void shouldImmediatelyThrowOnUserRefreshLimitExceeded() {
        // given
        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-rabobank-ob")
                        .withWireMockServer(WIRE_MOCK_TEST_SERVER)
                        .withWireMockFilePath(
                                path("nl-rabobank-ob-ais-user-refresh-limit-exceeded.aap"))
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(CONSENT_ID, "fffff")
                        .addPersistentStorageData(OAUTH_2_TOKEN, oauth2Token)
                        .withAgentTestModule(new RabobankWiremockTestModule())
                        .build();

        // expect
        assertThatThrownBy(agentWireMockRefreshTest::executeRefresh)
                .isInstanceOf(BankServiceException.class)
                .hasMessageContaining(
                        "calls for unattended requests has been exceeded for account with ID [asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf2]");
    }

    @SneakyThrows
    private static AgentsServiceConfiguration readConfiguration() {
        return AgentsServiceConfigurationReader.read(path("configuration.yml"));
    }

    private static String path(String filename) {
        return String.format(
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/rabobank/wiremock/resources/%s",
                filename);
    }
}
