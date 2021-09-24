package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixa.integration;

import io.dropwizard.configuration.ConfigurationException;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.module.SibsWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class CaixaAisWireMockTest {
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/openbanking/caixa/integration/resources/";
    private static final String CONFIGURATION_FILE_PATH = RESOURCE_PATH + "configuration.yml";
    private static final String CONTRACT_FILE_PATH = RESOURCE_PATH + "agent-contract.json";

    private final AgentsServiceConfiguration configuration = getConfiguration();

    private static AgentsServiceConfiguration getConfiguration() {
        try {
            return AgentsServiceConfigurationReader.read(CONFIGURATION_FILE_PATH);
        } catch (IOException | ConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testFullRefresh() throws Exception {
        // given
        final String filePath = RESOURCE_PATH + "caixa_pt_full_refresh.aap";
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.PT)
                        .withProviderName("pt-caixa-ob")
                        .withWireMockFilePath(filePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withAgentTestModule(new SibsWireMockTestModule())
                        .addCallbackData("accountSegment", "PERSONAL")
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .enableDataDumpForContractFile()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testAutoRefresh() throws Exception {
        // given
        final String filePath = RESOURCE_PATH + "caixa_pt_auto_refresh.aap";
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.PT)
                        .withProviderName("pt-caixa-ob")
                        .withWireMockFilePath(filePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withAgentTestModule(new SibsWireMockTestModule())
                        .addPersistentStorageData("sibs_manual_authentication_in_progress", "false")
                        .addPersistentStorageData("ACCOUNT_SEGMENT", "\"PERSONAL\"")
                        .addPersistentStorageData(
                                "CONSENT_ID",
                                "{\"consentId\":\"TEST_CONSENT\",\"consentCreated\":\"2020-09-22T10:00:48.699\"}")
                        .enableDataDumpForContractFile()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
