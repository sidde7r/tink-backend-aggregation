package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.integration;

import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.integration.module.FiduciaWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.enums.MarketCode;

public class FiduciaAgentWireMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/integration/resources/";

    private static String contractFilePath;

    private static AgentsServiceConfiguration configuration;

    @BeforeClass
    public static void setup() throws Exception {
        contractFilePath = BASE_PATH + "agent-contract.json";
        configuration = AgentsServiceConfigurationReader.read(BASE_PATH + "configuration.yml");
    }

    @Test
    public void testOneScaMethod() throws Exception {
        wiremockTest(BASE_PATH + "fiducia_mock_log.aap", "pushTan");
    }

    @Test
    public void testScaMethodSelection() throws Exception {
        wiremockTest(BASE_PATH + "fiducia_with_sca_method_selection_mock_log.aap", "chipTan");
    }

    private void wiremockTest(String wiremockFilePath, String chosenMethod) throws Exception {

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.DE, "de-vrbank-raiba-muc-sued-ob", wiremockFilePath)
                        .addCredentialField("psu-id", "dummy_psu_id")
                        .addCredentialField("password", "dummy_password")
                        .withConfigurationFile(configuration)
                        .addCallbackData("tanField", "dummy_otp_code")
                        .addCallbackData(chosenMethod, "dummy_otp_code")
                        .addCallbackData("selectAuthMethodField", "2")
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
