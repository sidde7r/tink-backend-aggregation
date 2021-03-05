package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.starling.wiremock;

import java.time.Clock;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.Token;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class StarlingAgentWireMockTest {

    private static final String PROVIDER_NAME = "uk-starling-oauth2";

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/starling/wiremock/resources/";

    private static final String AUTO_REFRESH_TRAFFIC = RESOURCES_PATH + "auto-refresh.aap";
    private static final String AUTO_REFRESH_DATA_CONTRACT = RESOURCES_PATH + "auto-refresh.json";

    private static final String NO_BUSINESS_ACCOUNTS_TRAFFIC =
            RESOURCES_PATH + "no-business-accounts.aap";
    private static final String NO_BUSINESS_ACCOUNTS_DATA_CONTRACT =
            RESOURCES_PATH + "no-business-accounts.json";

    private static final String INVALID_GRANT_TRAFFIC = RESOURCES_PATH + "invalid-grant.aap";

    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "config.yml";

    private static final String DUMMY_ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    private static final String DUMMY_EXPIRED_ACCESS_TOKEN = "DUMMY_EXPIRED_ACCESS_TOKEN";
    private static final String DUMMY_REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";
    private static final String TOKEN_TYPE_BEARER = "bearer";

    private RefreshableAccessToken refreshableAccessToken;
    private RefreshableAccessToken refreshableAccessTokenExpired;

    @Before
    public void setUp() throws Exception {
        this.refreshableAccessToken = createRefreshableAccessToken();
        this.refreshableAccessTokenExpired = createRefreshableAccessTokenExpired();
    }

    @Test
    public void shouldAutoRefreshSuccessfully() throws Exception {

        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(AUTO_REFRESH_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableAccessToken(refreshableAccessToken)
                        .build();

        // when
        test.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(AUTO_REFRESH_DATA_CONTRACT);
        test.assertExpectedData(expected);
    }

    @Test
    public void shouldNotReturnBusinessAccounts() throws Exception {

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(NO_BUSINESS_ACCOUNTS_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableAccessToken(refreshableAccessToken)
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(NO_BUSINESS_ACCOUNTS_DATA_CONTRACT);
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void shouldExitAuthAfterInvalidGrantResponse() throws Exception {
        // given
        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(INVALID_GRANT_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addRefreshableAccessToken(refreshableAccessTokenExpired)
                        .build();

        // expected
        Assertions.assertThatExceptionOfType(AgentPlatformAuthenticationProcessException.class)
                .isThrownBy(test::executeRefresh)
                .withMessage(
                        "RefreshTokenFailureError: Bank API HTTP status error. Unexpected status code. [APAG-1]");
    }

    private RefreshableAccessToken createRefreshableAccessToken() {
        Token expiredAccessToken = createAccessToken();
        Token refreshToken = createRefreshToken();

        return createRefreshableAccessToken(expiredAccessToken, refreshToken);
    }

    private RefreshableAccessToken createRefreshableAccessTokenExpired() {
        Token expiredAccessToken = createExpiredAccessToken();

        Token refreshToken = createRefreshToken();

        return createRefreshableAccessToken(expiredAccessToken, refreshToken);
    }

    private Token createAccessToken() {
        return Token.builder()
                .body(DUMMY_ACCESS_TOKEN)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(Clock.systemUTC().millis() / 1000, 100L)
                .build();
    }

    private Token createExpiredAccessToken() {
        return Token.builder()
                .body(DUMMY_EXPIRED_ACCESS_TOKEN)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(1000L, 0L)
                .build();
    }

    private Token createRefreshToken() {
        return Token.builder().body(DUMMY_REFRESH_TOKEN).build();
    }

    private RefreshableAccessToken createRefreshableAccessToken(
            Token expiredAccessToken, Token refreshToken) {
        return RefreshableAccessToken.builder()
                .accessToken(expiredAccessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
