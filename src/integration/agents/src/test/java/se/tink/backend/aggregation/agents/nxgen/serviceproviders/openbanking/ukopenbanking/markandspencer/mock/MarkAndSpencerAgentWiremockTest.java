package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.markandspencer.mock;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class MarkAndSpencerAgentWiremockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/markandspencer/mock/resources/";
    private static final String configFilePath = RESOURCES_PATH + "configuration.yml";
    private static final String wireMockServerFilePath = RESOURCES_PATH + "auto-auth.aap";
    private static final String OPEN_ID_ACCESS_TOKEN_STORAGE_KEY = "open_id_ais_access_token";
    private static final String TOKEN_TYPE_BEARER = "bearer";
    private static final String DUMMY_REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";
    private static final String DUMMY_EXPIRED_ACCESS_TOKEN = "DUMMY_EXPIRED_ACCESS_TOKEN";

    @Test
    public void shouldRefreshTokenSuccessfully() throws Exception {
        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName("uk-markandspencer-oauth2")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(AgentsServiceConfigurationReader.read(configFilePath))
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData(
                                OPEN_ID_ACCESS_TOKEN_STORAGE_KEY, createOpenIdExpiredAccessToken())
                        .addCallbackData("code", "DUMMY_CODE")
                        .build();

        // expected
        Assertions.assertThatCode(test::executeRefresh).doesNotThrowAnyException();
    }

    private String createOpenIdExpiredAccessToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.create(
                        TOKEN_TYPE_BEARER, DUMMY_EXPIRED_ACCESS_TOKEN, DUMMY_REFRESH_TOKEN, 0, 90));
    }
}
