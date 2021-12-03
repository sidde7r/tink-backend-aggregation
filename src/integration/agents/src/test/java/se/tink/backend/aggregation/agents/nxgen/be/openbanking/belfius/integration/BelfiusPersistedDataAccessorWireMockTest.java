package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.integration;

import static se.tink.libraries.enums.MarketCode.BE;

import lombok.SneakyThrows;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.AuthenticationConfigurationStep;
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
    private static final AgentContractEntity EXPECTED =
            new AgentContractEntitiesJsonFileParser()
                    .parseContractOnBasisOfFile(RESOURCE_PATH + "agent-contract.json");
    private static final String TOKEN_STORAGE_KEY = "oauth2_access_token";
    private static final String LOGICAL_ID_STORAGE_KEY = "logical_id";
    private static final String LOGICAL_ID = "f2533843-785d-4499-b92f-61711101feed";

    private final AgentsServiceConfiguration configuration = readConfiguration();

    @Test
    public void manualRefreshTest() throws Exception {
        // given
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                getCommonTestConfiguration("belfius_manual_refresh.aap")
                        .testFullAuthentication()
                        .withRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ALL)
                        .addCredentialField("iban", "BE39000000076000")
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(EXPECTED);
    }

    @Test
    public void autoAuthenticationOnlyTest() throws Exception {
        // given
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                getCommonTestConfiguration("belfius_auto_authentication_only.aap")
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData(TOKEN_STORAGE_KEY, getTokenValidFor(1000))
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();
    }

    @Test
    public void autoRefreshTest() throws Exception {
        // given
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                getCommonTestConfiguration("belfius_auto_refresh.aap")
                        .testAutoAuthentication()
                        .withRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ALL)
                        .addPersistentStorageData(TOKEN_STORAGE_KEY, getTokenValidFor(0))
                        .addPersistentStorageData(LOGICAL_ID_STORAGE_KEY, LOGICAL_ID)
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(EXPECTED);
    }

    @Test
    public void validSessionRefreshTest() throws Exception {
        // given
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                getCommonTestConfiguration("belfius_valid_session_refresh.aap")
                        .testAutoAuthentication()
                        .withRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ALL)
                        .addPersistentStorageData(TOKEN_STORAGE_KEY, getTokenValidFor(1000))
                        .addPersistentStorageData(LOGICAL_ID_STORAGE_KEY, LOGICAL_ID)
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(EXPECTED);
    }

    private AuthenticationConfigurationStep getCommonTestConfiguration(String wireMockFileName) {
        return AgentWireMockRefreshTest.nxBuilder()
                .withMarketCode(BE)
                .withProviderName("be-belfius-ob")
                .withWireMockServer(WIREMOCK_TEST_SERVER)
                .withWireMockFilePath(RESOURCE_PATH + wireMockFileName)
                .withConfigFile(configuration);
    }

    @SneakyThrows
    private AgentsServiceConfiguration readConfiguration() {
        return AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    private String getTokenValidFor(int validFor) {
        return SerializationUtils.serializeToString(
                OAuth2Token.create("Bearer", "DUMMY_ACCESS_TOKEN", "refreshToken", validFor));
    }
}
