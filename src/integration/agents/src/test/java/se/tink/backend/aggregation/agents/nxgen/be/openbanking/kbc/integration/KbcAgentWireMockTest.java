package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class KbcAgentWireMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/kbc/integration/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";
    private static final String PROVIDER_NAME = "be-kbc-ob";

    private AgentsServiceConfiguration configuration;

    @Before
    public void init() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    @Test
    public void testSuccessful() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "mock_log.aap";
        final String contractFilePath = BASE_PATH + "agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addCredentialField("iban", "BE39000000076000")
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
    public void shouldFailWithExpiredConsent() {
        // given
        final String wireMockFilePath = BASE_PATH + "expired_consent_log.app";
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData("oauth2_access_token", getToken())
                        .addPersistentStorageData("consentId", "dummy_consent_id")
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .addCredentialField("iban", "BE39000000076000")
                        .build();

        // when
        final Throwable thrown = catchThrowable(agentWireMockRefreshTest::executeRefresh);
        // then
        assertThat(thrown).isExactlyInstanceOf(AgentPlatformAuthenticationProcessException.class);
        AgentPlatformAuthenticationProcessException agentPlatformAuthenticationProcessException =
                (AgentPlatformAuthenticationProcessException) thrown;
        AgentBankApiError sourceAgentPlatformError =
                agentPlatformAuthenticationProcessException.getSourceAgentPlatformError();
        assertThat(sourceAgentPlatformError).isExactlyInstanceOf(SessionExpiredError.class);
    }

    @Test
    public void shouldFailWithExpiredConsentAfterTokenRefresh() {
        // given
        final String wireMockFilePath = BASE_PATH + "expired_token_expired_consent_log.app";
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData("oauth2_access_token", getExpiredToken())
                        .addPersistentStorageData("consentId", "dummy_consent_id")
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .addCredentialField("iban", "BE39000000076000")
                        .build();

        // when
        final Throwable thrown = catchThrowable(agentWireMockRefreshTest::executeRefresh);
        // then
        assertThat(thrown).isExactlyInstanceOf(AgentPlatformAuthenticationProcessException.class);
        AgentPlatformAuthenticationProcessException agentPlatformAuthenticationProcessException =
                (AgentPlatformAuthenticationProcessException) thrown;
        AgentBankApiError sourceAgentPlatformError =
                agentPlatformAuthenticationProcessException.getSourceAgentPlatformError();
        assertThat(sourceAgentPlatformError).isExactlyInstanceOf(SessionExpiredError.class);
    }

    @Test
    public void shouldFailWithInvalidConsent() {
        // given
        final String wireMockFilePath = BASE_PATH + "invalid_consent_log.app";
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.BE)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .addCredentialField("iban", "BE39000000076000")
                        .build();

        // when
        final Throwable thrown = catchThrowable(agentWireMockRefreshTest::executeRefresh);
        // then
        assertThat(thrown).isExactlyInstanceOf(AgentPlatformAuthenticationProcessException.class);
        AgentPlatformAuthenticationProcessException agentPlatformAuthenticationProcessException =
                (AgentPlatformAuthenticationProcessException) thrown;
        AgentBankApiError sourceAgentPlatformError =
                agentPlatformAuthenticationProcessException.getSourceAgentPlatformError();
        assertThat(sourceAgentPlatformError).isExactlyInstanceOf(AuthenticationError.class);
    }

    private String getToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.create("refreshToken", "accessToken", "refreshToken", 90));
    }

    private String getExpiredToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.create("refreshToken", "accessToken", "refreshToken", 0));
    }
}
