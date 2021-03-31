package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.mock;

import static se.tink.backend.agents.rpc.Field.Key.DATE_OF_BIRTH;
import static se.tink.backend.agents.rpc.Field.Key.MOBILENUMBER;
import static se.tink.libraries.enums.MarketCode.NO;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class NordeaNoMockServerAgentTest {
    @Test
    public void testRefresh() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/mock/resources/test-refresh.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/mock/resources/agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(NO, "no-nordea-bankid", wireMockFilePath)
                        .addCallbackData("name", "name")
                        .addCredentialField(MOBILENUMBER.getFieldKey(), "96325874")
                        .addCredentialField(DATE_OF_BIRTH.getFieldKey(), "121290")
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .dumpContentForContractFile()
                        .withHttpDebugTrace()
                        .withAgentModule(
                                new BankIdIframeAuthenticationControllerProviderMockModule())
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
