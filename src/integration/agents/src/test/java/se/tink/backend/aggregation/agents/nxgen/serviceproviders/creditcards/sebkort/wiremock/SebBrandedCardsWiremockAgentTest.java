package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.wiremock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class SebBrandedCardsWiremockAgentTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/creditcards/sebkort/wiremock/resources/configuration.yml";

    @Test
    public void testSasEurobonusMastercardSERefresh() throws Exception {
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/creditcards/sebkort/wiremock/resources/sas_eurobonus_mc_wiremock.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/creditcards/sebkort/wiremock/resources/sas_eurobonus_mc_contract.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.SE,
                                "se-saseurobonusmastercard-ob",
                                wireMockServerFilePath)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "dummyCode")
                        .withConfigurationFile(configuration)
                        .addRefreshableItems()
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        agentWireMockRefreshTest.executeRefresh();

        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
