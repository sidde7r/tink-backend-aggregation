package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics.wiremock;

import java.time.ZoneId;
import lombok.SneakyThrows;
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

public class IcsWiremockTest {

    private final WireMockTestServer wireMockTestServer = new WireMockTestServer();

    private final AgentContractEntity expectedRefreshData =
            new AgentContractEntitiesJsonFileParser()
                    .parseContractOnBasisOfFile(path("nl-ics-oauth2-refresh-contract.json"));

    private final long accessTokenExpiresInSeconds = 30;

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

    @Test
    @SneakyThrows
    public void shouldPerformManualRefresh() {

        // given
        AgentWireMockRefreshTest manualRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-ics-oauth2")
                        .withWireMockServer(wireMockTestServer)
                        .withWireMockFilePath(path("nl-ics-oauth2-manual-refresh.aap"))
                        .withConfigFile(configuration())
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "0000000000000")
                        .build();

        // when
        manualRefreshTest.executeRefresh();

        // then
        manualRefreshTest.assertExpectedData(expectedRefreshData);
    }

    @Test
    @SneakyThrows
    public void shouldPerformAutoRefresh() {

        // given
        AgentWireMockRefreshTest autoRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-ics-oauth2")
                        .withWireMockServer(wireMockTestServer)
                        .withWireMockFilePath(path("nl-ics-oauth2-auto-refresh.aap"))
                        .withConfigFile(configuration())
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(PersistentStorageKeys.OAUTH_2_TOKEN, accessToken)
                        .build();

        // when
        autoRefreshTest.executeRefresh();

        // then
        autoRefreshTest.assertExpectedData(expectedRefreshData);
    }

    @SneakyThrows
    private AgentsServiceConfiguration configuration() {
        return AgentsServiceConfigurationReader.read(path("nl-ics-oauth2-configuration.yml"));
    }

    private String path(String filename) {
        return String.format(
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/ics/wiremock/resources/%s",
                filename);
    }
}
