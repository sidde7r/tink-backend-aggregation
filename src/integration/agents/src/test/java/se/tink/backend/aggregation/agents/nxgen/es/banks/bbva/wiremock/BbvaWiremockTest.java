package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.wiremock;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.enums.MarketCode;

public class BbvaWiremockTest {
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/bbva/wiremock/resources/";

    private static final String PROVIDER_NAME = "es-bbva-password";
    private static final String USERNAME = "1234567L";
    private static final String PASSWORD = "123456";
    private static final String OTP_INPUT = "13371337";

    @Test
    @Ignore("still in progress. a lot of modifications")
    public void testFullRefreshWithSmsOtp() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "full-refresh.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "full-refresh.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserAvailableForInteraction(true);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.ES)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCredentialField(Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Key.PASSWORD.getFieldKey(), PASSWORD)
                        .addCallbackData(Field.Key.OTP_INPUT.getFieldKey(), OTP_INPUT)
                        .enableDataDumpForContractFile()
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
