package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.mock;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class BarclaysCorporateAgentWiremockTest {

    private static final String PROVIDER_NAME = "uk-barclays-corporate-ob";
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/barclays/mock/resources/";
    private static final String FULL_AUTH_TRAFFIC = RESOURCES_PATH + "full-auth.aap";
    private static final String AUTO_AUTH_FETCH_DATA_TRAFFIC =
            RESOURCES_PATH + "auto-auth-fetch-data.aap";
    private static final String AUTO_AUTH_FETCH_DATA_CONTRACT =
            RESOURCES_PATH + "auto-auth-fetch-data.json";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";

    private static final String TOKEN_TYPE_BEARER = "bearer";
    private static final String DUMMY_ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    private static final String DUMMY_REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";

    @Test
    public void shouldRunFullAuthenticationSuccessfully() throws Exception {
        // given
        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(FULL_AUTH_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("code", "DUMMY_ACCESS_TOKEN2")
                        .build();

        // expected
        assertThatCode(test::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void shouldRunAutoAuthenticationWithDataFetchSuccessfully() throws Exception {
        // Given
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(AUTO_AUTH_FETCH_DATA_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(
                                OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                createOpenIdAccessToken())
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(AUTO_AUTH_FETCH_DATA_CONTRACT);
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    private String createOpenIdAccessToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.create(
                        TOKEN_TYPE_BEARER, DUMMY_ACCESS_TOKEN, DUMMY_REFRESH_TOKEN, 599));
    }
}
