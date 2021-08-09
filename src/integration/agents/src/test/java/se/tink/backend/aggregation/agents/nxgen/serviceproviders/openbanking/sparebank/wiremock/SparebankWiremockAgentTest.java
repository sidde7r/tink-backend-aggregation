package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.wiremock;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.google.gson.Gson;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.util.AccountTestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.wiremock.module.SparebankWiremockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class SparebankWiremockAgentTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/wiremock/resources/configuration.yml";

    private static final String contractFilePath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/wiremock/resources/agent-contract.json";

    private static final AccountResponse SAMPLE_ACCOUNT_RESPONSE =
            AccountTestData.getAccountResponse();

    @Test
    public void testManualAuthentication() throws Exception {

        // given
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/wiremock/resources/manual-authentication-sparebank-ob.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NO)
                        .withProviderName("no-sparebank1sr-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .withAgentTestModule(new SparebankWiremockTestModule())
                        .addCallbackData("psu-id", "PSU_ID")
                        .addCallbackData("tpp-session-id", "SESSION_ID")
                        .build();

        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void testAutoRefresh() throws Exception {

        // given
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/wiremock/resources/auto-refresh-sparebank-ob.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        Gson gson = new Gson();
        String sampleAcount = gson.toJson(SAMPLE_ACCOUNT_RESPONSE);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NO)
                        .withProviderName("no-sparebank1sr-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withAgentTestModule(new SparebankWiremockTestModule())
                        .addPersistentStorageData("SESSION_ID", "XXXX_SESSION")
                        .addPersistentStorageData("PSU_ID", "XXXX_ID")
                        .addPersistentStorageData("CONSENT_CREATED_TIMESTAMP", 1628165339000L)
                        .addPersistentStorageData("accounts", sampleAcount)
                        .addPersistentStorageData("cards", CardResponse.empty())
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);
        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testManualRefresh() throws Exception {

        // given
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/wiremock/resources/manual-refresh-sparebank-ob.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        Gson gson = new Gson();
        String sampleAcount = gson.toJson(SAMPLE_ACCOUNT_RESPONSE);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NO)
                        .withProviderName("no-sparebank1sr-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withAgentTestModule(new SparebankWiremockTestModule())
                        .addCallbackData("psu-id", "PSU_ID")
                        .addCallbackData("tpp-session-id", "SESSION_ID")
                        .addCallbackData("toDate", LocalDateTime.now().toString())
                        .addPersistentStorageData("SESSION_ID", "XXXX_SESSION")
                        .addPersistentStorageData("PSU_ID", "XXXX_ID")
                        .addPersistentStorageData(
                                "CONSENT_CREATED_TIMESTAMP",
                                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                        .addPersistentStorageData("accounts", sampleAcount)
                        .addPersistentStorageData("cards", CardResponse.empty())
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);
        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
