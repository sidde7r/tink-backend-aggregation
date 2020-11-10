package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.monzo.mock;

import java.time.LocalDateTime;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants.StorageKeys;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class MonzoAgentWiremockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/monzo/mock/resources/";
    static final String configFilePath = RESOURCES_PATH + "configuration.yml";

    private static final String PROVIDER_NAME = "uk-monzo-oauth2";
    private static final String OAUTH_TOKEN =
            "{\"expires_in\" : 999999999, \"issuedAt\": 1598516000, \"token_type\":\"bearer\",  \"access_token\":\"DUMMY_OAUTH2_TOKEN\", \"refreshToken\":\"DUMMY_REFRESH_TOKEN\"}";

    @Test
    public void manualRefreshAll() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "manual-refresh-all.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "manual-refresh-all-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.UK, PROVIDER_NAME, wireMockServerFilePath)
                        .withConfigurationFile(
                                AgentsServiceConfigurationReader.read(configFilePath))
                        .dumpContentForContractFile()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "DUMMY_ACCESS_TOKEN2")
                        .withHttpDebugTrace()
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    /**
     * * https://docs.monzo.com/#parties Endpoint /party expires 5 min after last SCA In case of
     * auto authentication data will be retrieved from persistent storage
     */
    @Test
    public void restoreIdentityData() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "restore-identity-data.aap";
        final String wireMockContractFilePath =
                RESOURCES_PATH + "restore-identity-data-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final String identityDataV31Entity =
                "{\"PartyId\": \"user_11119x3OpXVihQdEO1EoXC\", \"Name\": \"John Tinker\", \"FullLegalName\":\"John The Tinker2\"}";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.UK, PROVIDER_NAME, wireMockServerFilePath)
                        .withConfigurationFile(
                                AgentsServiceConfigurationReader.read(configFilePath))
                        .dumpContentForContractFile()
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(
                                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                OAUTH_TOKEN)
                        .addPersistentStorageData(
                                UkOpenBankingV31Constants.PersistentStorageKeys.LAST_SCA_TIME,
                                LocalDateTime.now().minusMinutes(6).toString())
                        .addPersistentStorageData(
                                StorageKeys.RECENT_IDENTITY_DATA, identityDataV31Entity)
                        .withHttpDebugTrace()
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
