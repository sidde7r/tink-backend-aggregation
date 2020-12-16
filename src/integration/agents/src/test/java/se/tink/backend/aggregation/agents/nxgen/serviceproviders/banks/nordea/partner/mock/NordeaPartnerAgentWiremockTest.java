package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.mock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.mock.module.NordeaPartnerMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.enums.MarketCode;

public class NordeaPartnerAgentWiremockTest {
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/nordea/partner/mock/resources/";

    private AgentWireMockRefreshTest buildRefreshTest(
            String providerName,
            String wireMockFilePath,
            AgentsServiceConfiguration configuration) {
        return AgentWireMockRefreshTest.builder(MarketCode.SE, providerName, wireMockFilePath)
                .withConfigurationFile(configuration)
                .withAgentModule(new NordeaPartnerMockTestModule())
                .addCredentialField(
                        Key.USERNAME.getFieldKey(), "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();
    }

    @Test
    public void testRefresh() throws Exception {

        // given
        final String wireMockFilePath = RESOURCES_PATH + "refresh.aap";
        final String contractFilePath = RESOURCES_PATH + "contract.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(RESOURCES_PATH + "configuration.yml");

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildRefreshTest("se-nordeapartner-jwt", wireMockFilePath, configuration);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
