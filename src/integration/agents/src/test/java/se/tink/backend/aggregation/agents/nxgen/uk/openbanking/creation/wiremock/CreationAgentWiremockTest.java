package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.creation.wiremock;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class CreationAgentWiremockTest {

    private static final String PROVIDER_NAME = "uk-creation-ob";
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/creation/wiremock/resources/";
    private static final String CONFIG_FILE_PATH = RESOURCES_PATH + "configuration.yml";
    private static final String DUMMY_OAUTH2_TOKEN =
            "{\"tokenType\":\"bearer\",\"accessToken\":\"DUMMY_ACCESS_TOKEN\",\"refreshToken\":\"DUMMY_REFRESH_TOKEN\",\"idToken\":null,\"expiresInSeconds\":99999999999,\"refreshExpiresInSeconds\":99999999999,\"issuedAt\":1598516000}";
    private static final String AIS_ACCESS_TOKEN_KEY = "open_id_ais_access_token";
    private static final String CODE_PARAM = "code";
    private static final String DUMMY_CODE = "DUMMY_CODE";

    @Test
    public void testAutoRefresh() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "manual-refresh-all.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "manual-refresh-all-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIG_FILE_PATH))
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.CREDITCARD_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.CREDITCARD_TRANSACTIONS)
                        .addPersistentStorageData(AIS_ACCESS_TOKEN_KEY, DUMMY_OAUTH2_TOKEN)
                        .build();

        // when
        test.executeRefresh();

        // then
        test.assertExpectedData(expected);
    }

    @Test
    public void shouldRunFullAuthSuccessfully() throws Exception {
        final String wireMockServerFilePath = RESOURCES_PATH + "full-auth.aap";

        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIG_FILE_PATH))
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData(CODE_PARAM, DUMMY_CODE)
                        .enableHttpDebugTrace()
                        .build();

        // expected
        Assertions.assertThatCode(test::executeRefresh).doesNotThrowAnyException();
    }
}
