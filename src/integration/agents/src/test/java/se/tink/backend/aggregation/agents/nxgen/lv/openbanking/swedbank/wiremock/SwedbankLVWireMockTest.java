package se.tink.backend.aggregation.agents.nxgen.lv.openbanking.swedbank.wiremock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.lv.openbanking.swedbank.wiremock.module.SwedbankLVWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class SwedbankLVWireMockTest {

    @Test
    public void testRefresh() throws Exception {

        // given
        final String CONFIGURATION_PATH =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/lv/openbanking/swedbank/wiremock/resources/configuration.yml";

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/lv/openbanking/swedbank/wiremock/resources/swedbank_lv_mock_log.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/lv/openbanking/swedbank/wiremock/resources/swedbank_lv_contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.LV)
                        .withProviderName("lv-swedbank-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCredentialField(Key.USERNAME.getFieldKey(), "1234567")
                        .addCredentialField(Key.NATIONAL_ID_NUMBER.getFieldKey(), "200012121213")
                        .withAgentTestModule(new SwedbankLVWireMockTestModule())
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);
        // when
        agentWireMockRefreshTest.executeRefresh();
        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
