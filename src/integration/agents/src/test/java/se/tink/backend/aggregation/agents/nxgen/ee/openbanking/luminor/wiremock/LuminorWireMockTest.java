package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.luminor.wiremock;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

@Ignore
public class LuminorWireMockTest {
    @Test
    public void testRefresh() throws Exception {

        // given
        final String configurationPath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/luminor/wiremock/resources/configuration.yml";
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/luminor/wiremock/resources/ee-luminor-ob-ais.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.EE)
                        .withProviderName("ee-luminor-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "dummyCode")
                        .addCredentialField("ibans", "iban01")
                        .build();

        /* final AgentContractEntity expected =
        new AgentContractEntitiesJsonFileParser()
            .parseContractOnBasisOfFile(contractFilePath);*/

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        //  agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
