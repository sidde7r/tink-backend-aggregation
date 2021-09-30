package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.integration;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.integration.module.ArgentaAgentWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

@RunWith(JUnitParamsRunner.class)
public class ArgentaAgentWireMockTest {
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/argenta/integration/resources/";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";
    private static final String contractFilePath = RESOURCES_PATH + "agent-contract.json";

    @Test
    public void test() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "argenta_full_refresh_mock_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName("be-argenta-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "DUMMY_CODE")
                        .withAgentTestModule(new ArgentaAgentWireMockTestModule())
                        .enableDataDumpForContractFile()
                        .addCredentialField(
                                ArgentaConstants.CredentialKeys.IBAN, "BE92199493742823")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    @Parameters({
        "argenta_refresh_auto_mock_log.aap, 0",
        "argenta_refresh_auto_with_valid_session_mock_log.aap, 899"
    })
    public void testAutoRefresh(String wireMockFile, long accessExpiresInSeconds) throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + wireMockFile;

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName("be-argenta-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withAgentTestModule(new ArgentaAgentWireMockTestModule())
                        .enableDataDumpForContractFile()
                        .addPersistentStorageData(
                                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN,
                                OAuth2Token.create(
                                        "bearer",
                                        "test_access_token",
                                        "test_refresh_token",
                                        accessExpiresInSeconds))
                        .addPersistentStorageData("CONSENT_ID", "1112222a1bbbb12c3d4ee123")
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
