package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.mock;

import static se.tink.libraries.enums.MarketCode.DE;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public class Xs2aMockServerAgentTest {
    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/xs2adevelopers/mock/resources/configuration.yml";

    @Test
    public void testRefreshWithPaginationError() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/xs2adevelopers/mock/resources/commerz-paging-with-error-refresh.aap";

        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/xs2adevelopers/mock/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(DE, "de-commerzbank-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "woot")
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
