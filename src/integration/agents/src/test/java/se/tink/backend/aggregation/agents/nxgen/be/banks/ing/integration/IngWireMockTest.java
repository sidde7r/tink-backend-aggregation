package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.integration.module.IngWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.enums.MarketCode;

public class IngWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/ing/integration/resources/";

    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";

    @Test
    public void test() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "ing_mock_log.aap";
        final String contractFilePath = RESOURCES_PATH + "agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                createAgentWireMockRefreshTest(wireMockFilePath);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testError() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "ing_mock_log_err.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                createAgentWireMockRefreshTest(wireMockFilePath);

        // when
        final Throwable thrown = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.SESSION_TERMINATED");
    }

    private AgentWireMockRefreshTest createAgentWireMockRefreshTest(String wireMockFilePath)
            throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        return AgentWireMockRefreshTest.builder(
                        MarketCode.BE, "be-ing-cardreader", wireMockFilePath)
                .withConfigurationFile(configuration)
                .addCredentialField("username", "DUMMY_USER")
                .addCredentialField("cardId", "DUMMY_CARD_NUMBER")
                .addCallbackData("otpinput", "DUMMY_OTP")
                .addCallbackData("signcodeinput", "DUMMY_SIGN_CODE")
                .withAgentModule(new IngWireMockTestModule())
                .build();
    }
}
