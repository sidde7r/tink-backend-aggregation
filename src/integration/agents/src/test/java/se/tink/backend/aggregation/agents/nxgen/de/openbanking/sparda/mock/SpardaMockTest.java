package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static se.tink.libraries.enums.MarketCode.DE;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UserAvailability;

public class SpardaMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/sparda/mock/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";

    @Test
    public void testFullAuthAndRefresh() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "fullAuthWithRefresh.aap";
        final String contractFilePath = BASE_PATH + "commonContract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-sparda-nurnberg-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .enableDataDumpForContractFile()
                        .addCallbackData("code", "test_callback_code")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testAutoAuthAndRefreshWithoutUserPresent() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "autoAuthWithRefreshNoUser.aap";
        final String contractFilePath = BASE_PATH + "commonContract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(false);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-sparda-nurnberg-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .enableDataDumpForContractFile()
                        .withUserAvailability(userAvailability)
                        .addPersistentStorageData("consentId", "test_consent_id")
                        .addPersistentStorageData(
                                "oauth2_access_token",
                                OAuth2Token.createBearer(
                                        "test_access_token_old", "test_refresh_token", 0))
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testAutoAuthAndRefreshWithTokenExpiringDuringRefresh() throws Exception {
        // given
        final String wireMockFilePath =
                BASE_PATH + "autoAuthWithRefreshTokenExpiryDuringRefresh.aap";
        final String contractFilePath = BASE_PATH + "commonContract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(true);
        userAvailability.setOriginatingUserIp("127.0.0.1");

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-sparda-nurnberg-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .enableDataDumpForContractFile()
                        .withUserAvailability(userAvailability)
                        .addPersistentStorageData("consentId", "test_consent_id")
                        .addPersistentStorageData(
                                "oauth2_access_token",
                                OAuth2Token.createBearer(
                                        "test_access_token_old", "test_refresh_token", 15000))
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
