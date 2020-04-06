package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.mock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.enums.MarketCode;

public class LansforsakringarMockServerAgentTest {

    private static final String SSN = "197101019876";

    @Test
    public void testRefresh() throws Exception {
        // Given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/lansforsakringar/mock/resources/lansforsakringar-refresh-traffic.aap";

        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/lansforsakringar/mock/resources/agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.SE, "lansforsakringar-bankid", wireMockFilePath)
                        .addCredentialField(Key.USERNAME.getFieldKey(), SSN)
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
