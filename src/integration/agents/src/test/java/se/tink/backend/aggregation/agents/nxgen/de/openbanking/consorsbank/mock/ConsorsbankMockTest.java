package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static se.tink.libraries.enums.MarketCode.DE;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccountReferenceEntity;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UserAvailability;

public class ConsorsbankMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/consorsbank/mock/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";

    @Test
    public void testFullAuthAndRefresh() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "fullAuthWithRefresh.aap";
        final String contractFilePath = BASE_PATH + "fullAuthWithRefreshContract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-consorsbank-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .enableDataDumpForContractFile()
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testAutoAuthAndRefreshWithoutUserPresent() throws Exception {
        // given
        final String wireMockFilePath = BASE_PATH + "autoAuthWithRefreshNoUser.aap";
        final String contractFilePath = BASE_PATH + "autoAuthWithRefreshNoUserContract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(false);

        List<AccountReferenceEntity> accessAllowed = new ArrayList<>();
        accessAllowed.add(new AccountReferenceEntity("DE1234", null));
        AccessEntity accessEntity =
                AccessEntity.builder().accounts(accessAllowed).balances(accessAllowed).build();

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-consorsbank-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .enableDataDumpForContractFile()
                        .withUserAvailability(userAvailability)
                        .addPersistentStorageData("consentId", "test_consent_id")
                        .addPersistentStorageData("consentAccess", accessEntity)
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
