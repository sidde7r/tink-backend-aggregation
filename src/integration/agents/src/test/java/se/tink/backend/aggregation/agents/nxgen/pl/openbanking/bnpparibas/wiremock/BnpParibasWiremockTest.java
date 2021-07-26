package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.bnpparibas.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.pl.openbanking.bnpparibas.wiremock.module.BnpParibasWiremockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class BnpParibasWiremockTest {

    static final String configurationPath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pl/openbanking/bnpparibas/wiremock/resources/configuration.yml";

    @Test
    public void testFullRefresh() throws Exception {
        // given
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pl/openbanking/bnpparibas/wiremock/resources/pl-bnpparibas-ob-ais.aap";

        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pl/openbanking/bnpparibas/wiremock/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.PL)
                        .withProviderName("pl-bnpparibas-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "dummyCode")
                        .withAgentTestModule(new BnpParibasWiremockTestModule())
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
