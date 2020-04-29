package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.mock;

import static se.tink.backend.agents.rpc.Field.Key.PASSWORD;
import static se.tink.backend.agents.rpc.Field.Key.USERNAME;
import static se.tink.libraries.enums.MarketCode.DE;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;

public class HvbAgentWireMockTest {

    @Test
    public void testRefresh() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/banks/hvb/mock/resources/hvb_mock_log.aap";

        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/banks/hvb/mock/resources/agent-contract.json";

        String givenUserName = "1122334455";
        String givenPassword = "pass";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                DE, "de-hypovereinsbank-password", wireMockFilePath)
                        .addCredentialField(USERNAME.getFieldKey(), givenUserName)
                        .addCredentialField(PASSWORD.getFieldKey(), givenPassword)
                        .withHttpDebugTrace()
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
