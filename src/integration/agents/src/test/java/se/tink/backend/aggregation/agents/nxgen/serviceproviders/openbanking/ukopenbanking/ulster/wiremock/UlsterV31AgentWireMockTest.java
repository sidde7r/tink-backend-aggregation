package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ulster.wiremock;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class UlsterV31AgentWireMockTest {

    private static final String PROVIDER_NAME = "uk-ulster-oauth2";

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ulster/wiremock/resources/";

    private static final String FULL_AUTH_TRAFFIC = RESOURCES_PATH + "full-auth.aap";
    private static final String AUTO_AUTH_FETCH_DATA_TRAFFIC =
            RESOURCES_PATH + "auto-auth-fetch-data.aap";
    private static final String AUTO_AUTH_FETCH_DATA_CONTRACT =
            RESOURCES_PATH + "auto-auth-fetch-data.json";
    private static final String REFRESH_TOKEN_TRAFFIC = RESOURCES_PATH + "refresh-token.aap";
    private static final String AUTO_AUTH_CONSENT_ID_STORED_REVOKED_TRAFFIC =
            RESOURCES_PATH + "auto-auth-consent-id-stored-revoked.aap";

    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "config.yml";

    private static final String CODE_PARAM = "code";
    private static final String DUMMY_CODE = "DUMMY_CODE";

    private static final String TOKEN_TYPE_BEARER = "bearer";
    private static final String DUMMY_ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    private static final String DUMMY_EXPIRED_ACCESS_TOKEN = "DUMMY_EXPIRED_ACCESS_TOKEN";
    private static final String DUMMY_REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";
    private static final String OPEN_ID_ACCESS_TOKEN_STORAGE_KEY = "open_id_ais_access_token";

    private static final String DUMMY_CONSENT_ID = "DUMMY_CONSENT_ID";
    private static final String AIS_ACCOUNT_CONSENT_ID = "ais_account_consent_id";

    @Test
    public void shouldRunFullAuthSuccessfully() throws Exception {

        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(FULL_AUTH_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData(CODE_PARAM, DUMMY_CODE)
                        .enableHttpDebugTrace()
                        .build();

        // expected
        Assertions.assertThatCode(test::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void shouldRunAutoAuthWithDataRefreshSuccessfully() throws Exception {

        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(AUTO_AUTH_FETCH_DATA_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        // TODO: find user with more items!
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.SAVING_ACCOUNTS)
                        .addPersistentStorageData(
                                OPEN_ID_ACCESS_TOKEN_STORAGE_KEY, createOpenIdAccessToken())
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .build();

        // when
        test.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(AUTO_AUTH_FETCH_DATA_CONTRACT);
        test.assertExpectedData(expected);
    }

    @Test
    public void shouldRefreshTokenSuccessfully() throws Exception {
        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(REFRESH_TOKEN_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData(
                                OPEN_ID_ACCESS_TOKEN_STORAGE_KEY, createOpenIdExpiredAccessToken())
                        .addCallbackData(CODE_PARAM, DUMMY_CODE)
                        .enableHttpDebugTrace()
                        .build();

        // expected
        Assertions.assertThatCode(test::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowSessionExpRevokedConsent() throws Exception {
        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(AUTO_AUTH_CONSENT_ID_STORED_REVOKED_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData(
                                OPEN_ID_ACCESS_TOKEN_STORAGE_KEY, createOpenIdAccessToken())
                        .addPersistentStorageData(AIS_ACCOUNT_CONSENT_ID, DUMMY_CONSENT_ID)
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .enableWireMockServerLogs()
                        .build();

        // expected
        assertThatExceptionOfType(SessionException.class)
                .isThrownBy(test::executeRefresh)
                .withMessage("Invalid consent status. Expiring the session.");
    }

    private String createOpenIdAccessToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.create(
                        TOKEN_TYPE_BEARER, DUMMY_ACCESS_TOKEN, DUMMY_REFRESH_TOKEN, 599));
    }

    private String createOpenIdExpiredAccessToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.create(
                        TOKEN_TYPE_BEARER, DUMMY_EXPIRED_ACCESS_TOKEN, DUMMY_REFRESH_TOKEN, 0, 90));
    }
}
