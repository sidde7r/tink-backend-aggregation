package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.AuthenticationConfigurationStep;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.integration.module.IngAgentWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class IngAgentWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/ing/integration/resources/";
    private AgentsServiceConfiguration configuration;

    @Before
    public void init() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(RESOURCES_PATH + "configuration.yml");
    }

    @Test
    public void shouldAuthenticateNewUser() {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "ing_manual_first_authorization.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                prepareBasicWiremockConfiguration(wireMockFilePath)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("code", "DUMMY_CODE")
                        .withAgentTestModule(new IngAgentWireMockTestModule())
                        .build();

        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void shouldAuthenticateExistingUser() {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "ing_manual_next_authorization.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                prepareBasicWiremockConfiguration(wireMockFilePath)
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .withAgentTestModule(new IngAgentWireMockTestModule())
                        .addPersistentStorageData(
                                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN,
                                OAuth2Token.create(
                                        "bearer", "test_access_token", "test_refresh_token", 899))
                        .build();
        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void shouldRefreshManually() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "ing_manual_refresh.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                prepareBasicWiremockConfiguration(wireMockFilePath)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withAgentTestModule(new IngAgentWireMockTestModule())
                        .addPersistentStorageData(
                                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN,
                                OAuth2Token.create(
                                        "bearer", "test_access_token", "test_refresh_token", 8))
                        .addPersistentStorageData("CLIENT_ID", "123_CLIENT_ID")
                        .build();

        final AgentContractEntity expectedData =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(
                                RESOURCES_PATH + "agent-contract-manual-refresh.json");

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expectedData);
    }

    @Test
    public void shouldRefreshAutomatically() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "ing_auto_refresh.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                prepareBasicWiremockConfiguration(wireMockFilePath)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withAgentTestModule(new IngAgentWireMockTestModule())
                        .addPersistentStorageData(
                                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN,
                                OAuth2Token.create(
                                        "bearer", "test_access_token", "test_refresh_token", 8))
                        .addPersistentStorageData("CLIENT_ID", "123_CLIENT_ID")
                        .build();

        final AgentContractEntity expectedData =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(
                                RESOURCES_PATH + "agent-contract-auto-refresh.json");

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expectedData);
    }

    private AuthenticationConfigurationStep prepareBasicWiremockConfiguration(
            String wireMockFilePath) {
        return AgentWireMockRefreshTest.nxBuilder()
                .withMarketCode(MarketCode.BE)
                .withProviderName("be-ing-ob")
                .withWireMockFilePath(wireMockFilePath)
                .withConfigFile(configuration);
    }
}
