package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.integration;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.integration.module.FiduciaWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.enums.MarketCode;

public class FiduciaAgentWireMockTest {

    @Test
    public void test() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/integration/resources/fiducia_mock_log.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/integration/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/integration/resources/configuration.yml");

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.DE, "de-vrbank-raiba-muc-sued-ob", wireMockFilePath)
                        .addCredentialField("psu-id", "dummy_psu_id")
                        .addCredentialField("password", "dummy_password")
                        .withConfigurationFile(configuration)
                        .addCallbackData("otpinput", "dummy_otp_code")
                        .withAgentModule(new FiduciaWireMockTestModule())
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
