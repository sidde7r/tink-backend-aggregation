package se.tink.backend.aggregation.agents.nxgen.fr.banks.hellobank.integration;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.enums.MarketCode;

public class HelloBankWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/hellobank/integration/resources/";

    private static final String CONTRACT_FILE_PATH = RESOURCES_PATH + "agent-contract.json";

    @Test
    public void testAuthenticationAndDataFetch() throws Exception {
        createAgentWireMockRefreshTestAndRunTest("hellobank_mock_log.aap");
    }

    @Test
    public void testDataFetchForAlreadyAuthenticated() throws Exception {
        createAgentWireMockRefreshTestAndRunTest("hellobank_mock_log_no_auth.aap");
    }

    private void createAgentWireMockRefreshTestAndRunTest(String wireMockFilePath)
            throws Exception {
        // given
        final String wireMockFileFullPath = RESOURCES_PATH + wireMockFilePath;

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.FR, "fr-hellobank-password", wireMockFileFullPath)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "DUMMY_USER")
                        .addCredentialField(Field.Key.PASSWORD.getFieldKey(), "012345")
                        .withHttpDebugTrace()
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
