package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.vanquis.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class VanquisAgentWiremockTest {

    private static final String PROVIDER_NAME = "uk-vanquis-ob";
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/vanquis/wiremock/resources/";

    private static final String AUTO_AUTH_FETCH_DATA_TRAFFIC =
            RESOURCES_PATH + "auto-auth-fetch-data.aap";
    private static final String AUTO_AUTH_FETCH_DATA_CONTRACT =
            RESOURCES_PATH + "auto-auth-fetch-data.json";

    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "config.yml";

    public static final String AIS_ACCESS_TOKEN_KEY = "open_id_ais_access_token";
    private static final String DUMMY_OAUTH2_TOKEN =
            "{\"tokenType\":\"bearer\",\"accessToken\":\"DUMMY_ACCESS_TOKEN\",\"refreshToken\":\"DUMMY_REFRESH_TOKEN\",\"idToken\":null,\"expiresInSeconds\":99999999999,\"refreshExpiresInSeconds\":99999999999,\"issuedAt\":1598516000}";

    @Test
    public void shouldRunAutoAuthWithDataRefreshSuccessfully() throws Exception {
        // given
        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(AUTO_AUTH_FETCH_DATA_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.CREDITCARD_ACCOUNTS)
                        .addPersistentStorageData(AIS_ACCESS_TOKEN_KEY, DUMMY_OAUTH2_TOKEN)
                        .build();

        // when
        test.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(AUTO_AUTH_FETCH_DATA_CONTRACT);
        test.assertExpectedData(expected);
    }
}
