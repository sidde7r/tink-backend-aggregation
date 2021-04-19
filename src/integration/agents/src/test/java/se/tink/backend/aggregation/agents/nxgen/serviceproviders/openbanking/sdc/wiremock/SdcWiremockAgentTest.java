package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class SdcWiremockAgentTest {
    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sdc/wiremock/resources/configuration.yml";

    @Test
    public void testSparekassenKronjyllandDKRefresh() throws Exception {
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sdc/wiremock/resources/dk_sparekassenkronjylland.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sdc/wiremock/resources/dk_sparekassenkronjylland_contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildWiremockRefreshTest(
                        wireMockServerFilePath, MarketCode.DK, "dk-sparekassenkronjylland-ob");

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        agentWireMockRefreshTest.executeRefresh();
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testBergNORefresh() throws Exception {
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sdc/wiremock/resources/no_berg.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sdc/wiremock/resources/no_berg_contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildWiremockRefreshTest(wireMockServerFilePath, MarketCode.NO, "no-berg-ob");

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        agentWireMockRefreshTest.executeRefresh();
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    private AgentWireMockRefreshTest buildWiremockRefreshTest(
            String wireMockServerFilePath, MarketCode marketCode, String providerName)
            throws Exception {

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        return AgentWireMockRefreshTest.builder(marketCode, providerName, wireMockServerFilePath)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addCallbackData("code", "dummyCode")
                .withConfigurationFile(configuration)
                .addRefreshableItems()
                .build();
    }
}
