package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.hsbc.mock;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.enums.MarketCode;

public class HsbcKineticAgentWiremockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/hsbc/mock/resources/";
    private static final String configFilePath = RESOURCES_PATH + "configuration.yml";
    private static final String PROVIDER_NAME = "uk-hsbc-kinetic-ob";

    private static final Function<LocalDateTime, String> VALID_OAUTH2_TOKEN =
            localDateTime ->
                    String.format(
                            "{\"expires_in\" : 0, \"issuedAt\": %s, \"tokenType\":\"bearer\", \"refreshToken\":\"DUMMY_REFRESH_TOKEN\",  \"accessToken\":\"DUMMY_ACCESS_TOKEN\"}",
                            localDateTime.toEpochSecond(ZoneOffset.UTC));

    @Test
    public void manualAuthentication() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "manual-auth.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "manual-auth-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(AgentsServiceConfigurationReader.read(configFilePath))
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCallbackData("code", "DUMMY_ACCESS_TOKEN2")
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    // TODO finish when appear full flow in Kibana
    @Test
    public void manualRefreshAccount() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "manual-refresh-account.aap";
        final String wireMockContractFilePath =
                RESOURCES_PATH + "manual-refresh-account-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(AgentsServiceConfigurationReader.read(configFilePath))
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData(
                                OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                VALID_OAUTH2_TOKEN.apply(LocalDateTime.now().plusHours(1)))
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
