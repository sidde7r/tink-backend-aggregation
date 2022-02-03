package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.rbs.integration;

import java.util.Set;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RbsUniversalAgentWiremockTest {

    private static final WireMockTestServer WIREMOCK_TEST_SERVER = new WireMockTestServer(true);
    private static final String PROVIDER_NAME = "uk-rbs-universal-ob";
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/rbs/integration/resources/";

    private static final String AUTO_AUTH_FETCH_DATA_TRAFFIC =
            RESOURCES_PATH + "auto-auth-fetch-data.aap";
    private static final String AUTO_AUTH_FETCH_DATA_CONTRACT =
            RESOURCES_PATH + "auto-auth-fetch-data.json";

    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";
    private static final String TOKEN_TYPE_BEARER = "bearer";
    private static final String DUMMY_ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    private static final String DUMMY_REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";
    private static final String OPEN_ID_ACCESS_TOKEN_STORAGE_KEY = "open_id_ais_access_token";

    @Test
    public void shouldRunAutoAuthWithDataRefreshSuccessfully() throws Exception {
        // given
        Set<RefreshableItem> itemsExpectedToBeRefreshed = RefreshableItem.REFRESHABLE_ITEMS_ALL;

        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockServer(WIREMOCK_TEST_SERVER)
                        .withWireMockFilePath(AUTO_AUTH_FETCH_DATA_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        .withRefreshableItems(itemsExpectedToBeRefreshed)
                        .addPersistentStorageData(
                                OPEN_ID_ACCESS_TOKEN_STORAGE_KEY, createOpenIdAccessToken())
                        .build();

        // when
        test.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(AUTO_AUTH_FETCH_DATA_CONTRACT);
        test.assertExpectedData(expected);
    }

    private String createOpenIdAccessToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.create(
                        TOKEN_TYPE_BEARER, DUMMY_ACCESS_TOKEN, DUMMY_REFRESH_TOKEN, 599));
    }
}
