package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class VolksbankWiremockTest {

    private static final String basePath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/";

    @Test
    public void testAsnBankRefresh() throws Exception {

        // given
        final String configurationPath =
                basePath + "volksbank/wiremock/resources/asnbank-config.yml";
        final String wireMockServerFilePath =
                basePath + "volksbank/wiremock/resources/nl-asnbank-ob-ais.aap";

        final String contractFilePath =
                basePath + "volksbank/wiremock/resources/asnbank-agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-asnbank-oauth2")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "dummyCode")
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
    public void testSnsBankRefresh() throws Exception {

        // given
        final String configurationPath =
                basePath + "volksbank/wiremock/resources/snsbank-config.yml";
        final String wireMockServerFilePath =
                basePath + "volksbank/wiremock/resources/nl-snsbank-ob-ais.aap";

        final String contractFilePath =
                basePath + "volksbank/wiremock/resources/snsbank-agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-snsbank-oauth2")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "dummyCode")
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
    public void testRegioBankRefresh() throws Exception {

        // given
        final String configurationPath =
                basePath + "volksbank/wiremock/resources/regiobank-config.yml";
        final String wireMockServerFilePath =
                basePath + "volksbank/wiremock/resources/nl-regiobank-ob-ais.aap";

        final String contractFilePath =
                basePath + "volksbank/wiremock/resources/regiobank-agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-regiobank-oauth2")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "dummyCode")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    /**
     * Volksbank agent has the same logic for handling expired tokens, it's redundant to have a test
     * for each provider.
     */
    @Test(expected = SessionException.class)
    public void testSnsBankRefreshTokenExpired() throws Exception {

        final String configurationPath =
                basePath + "volksbank/wiremock/resources/snsbank-config.yml";
        final String wireMockServerFilePath =
                basePath + "volksbank/wiremock/resources/nl-snsbank-ob-refresh-token-expired.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-snsbank-oauth2")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(
                                VolksbankConstants.Storage.CONSENT, "dummy_consent_id")
                        .addPersistentStorageData(
                                PersistentStorageKeys.OAUTH_2_TOKEN,
                                OAuth2Token.createBearer(
                                        "test_access_token", "test_refresh_token", 0))
                        .build();

        agentWireMockRefreshTest.executeRefresh();
    }
}
