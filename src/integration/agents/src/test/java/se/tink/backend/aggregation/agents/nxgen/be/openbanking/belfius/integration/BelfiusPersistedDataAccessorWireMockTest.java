package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.integration;

import static se.tink.libraries.enums.MarketCode.BE;

import lombok.SneakyThrows;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BelfiusPersistedDataAccessorWireMockTest {

    private static final WireMockTestServer WIREMOCK_TEST_SERVER = new WireMockTestServer(true);
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/belfius/integration/resources/";
    private static final String CONFIGURATION_PATH = RESOURCE_PATH + "configuration.yml";

    private final AgentsServiceConfiguration configuration = readConfiguration();

    @Test
    public void manualRefreshTest() throws Exception {
        // given
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(BE)
                        .withProviderName("be-belfius-ob")
                        .withWireMockServer(WIREMOCK_TEST_SERVER)
                        .withWireMockFilePath(RESOURCE_PATH + "belfius_manual_refresh.aap")
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .withRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ALL)
                        .addCredentialField("iban", "BE39000000076000")
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(RESOURCE_PATH + "agent-contract.json");

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void autoAuthenticationTest() throws Exception {
        // given
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(BE)
                        .withProviderName("be-belfius-ob")
                        .withWireMockServer(WIREMOCK_TEST_SERVER)
                        .withWireMockFilePath(RESOURCE_PATH + "belfius_auto_authentication.aap")
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData("oauth2_access_token", validToken())
                        .build();

        // expect
        agentWireMockRefreshTest.executeRefresh();
    }

    @SneakyThrows
    private AgentsServiceConfiguration readConfiguration() {
        return AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    private String validToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.create("refreshToken", "accessToken", "refreshToken", 1000));
    }
}
