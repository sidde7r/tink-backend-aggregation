package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.wiremock;

import java.time.ZoneId;
import lombok.SneakyThrows;
import org.junit.AfterClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class KnabWiremockTest {

    private static final WireMockTestServer WIREMOCK_TEST_SERVER = new WireMockTestServer();

    private static final String CONSENT_ID = "consent_id";

    private final long accessTokenExpiresInSeconds = 3600;

    private final long accessTokenIssuedAtInSeconds =
            new ConstantLocalDateTimeSource()
                            .getInstant(ZoneId.systemDefault())
                            .minusSeconds(accessTokenExpiresInSeconds + 1)
                            .toEpochMilli()
                    / 1000;

    private final OAuth2Token accessToken =
            OAuth2Token.createBearer(
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMDAwMDAwMDAwIiwibmFtZSI6Ik1vbnR5IFB5dGhvbiIsImlhdCI6MTAwMDAwMDAwMH0.EJNJYpNffFpBdGbGoUo7emARuRT1iu2mbavJhCUGRYw",
                    "0000000000000000000000000000000000000000000000000000000000000000",
                    null,
                    accessTokenExpiresInSeconds,
                    accessTokenIssuedAtInSeconds);

    @AfterClass
    public static void cleanUp() {
        WIREMOCK_TEST_SERVER.shutdown();
    }

    @Test
    @SneakyThrows
    public void shouldPerformManualRefresh() {

        // given
        AgentWireMockRefreshTest manualRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-knab-ob")
                        .withWireMockServer(WIREMOCK_TEST_SERVER)
                        .withWireMockFilePath(path("nl-knab-refresh-manual.aap"))
                        .withConfigFile(configuration())
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData(
                                "code",
                                "0000000000000000000000000000000000000000000000000000000000000000")
                        .build();

        // and
        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(path("nl-knab-refresh-contract.json"));

        // when
        manualRefreshTest.executeRefresh();

        // then
        manualRefreshTest.assertExpectedData(expected);
    }

    @Test
    @SneakyThrows
    public void shouldPerformAutoRefresh() {

        // given
        AgentWireMockRefreshTest autoRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-knab-ob")
                        .withWireMockServer(WIREMOCK_TEST_SERVER)
                        .withWireMockFilePath(path("nl-knab-refresh-auto.aap"))
                        .withConfigFile(configuration())
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(
                                CONSENT_ID, "00000000-0000-5000-0000-000000000000")
                        .addPersistentStorageData(PersistentStorageKeys.OAUTH_2_TOKEN, accessToken)
                        .build();

        // and
        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(path("nl-knab-refresh-contract.json"));

        // when
        autoRefreshTest.executeRefresh();

        // then
        autoRefreshTest.assertExpectedData(expected);
    }

    @Test
    @Deprecated
    @SneakyThrows
    public void shouldPerformAutoRefreshForDeprecatedConsentApiV1() {

        // given
        AgentWireMockRefreshTest autoRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-knab-ob")
                        .withWireMockServer(WIREMOCK_TEST_SERVER)
                        .withWireMockFilePath(
                                path("nl-knab-refresh-auto-with-deprecated-consent.aap"))
                        .withConfigFile(configuration())
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(
                                CONSENT_ID, "00000000-0000-5000-0000-000000000000")
                        .addPersistentStorageData(PersistentStorageKeys.OAUTH_2_TOKEN, accessToken)
                        .build();

        // and
        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(path("nl-knab-refresh-contract.json"));

        // when
        autoRefreshTest.executeRefresh();

        // then
        autoRefreshTest.assertExpectedData(expected);
    }

    @SneakyThrows
    private AgentsServiceConfiguration configuration() {
        return AgentsServiceConfigurationReader.read(path("nl-knab-configuration.yml"));
    }

    private String path(String filename) {
        return String.format(
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/knab/wiremock/resources/%s",
                filename);
    }
}
