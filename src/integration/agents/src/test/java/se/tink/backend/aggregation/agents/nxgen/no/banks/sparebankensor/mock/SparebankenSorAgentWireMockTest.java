package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.mock;

import static se.tink.backend.agents.rpc.Field.Key.MOBILENUMBER;
import static se.tink.backend.agents.rpc.Field.Key.USERNAME;
import static se.tink.libraries.enums.MarketCode.NO;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SparebankenSorAgentWireMockTest {

    @Test
    public void testRefresh() throws Exception {

        // given
        final String encapMockFilePath =
                "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/utils/authentication/encap/mock/resources/encap_mock_log.aap";

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebankensor/mock/resources/sparebanken_sor_mock_log.aap";

        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebankensor/mock/resources/agent-contract.json";

        String givenUserName = "01059344907";
        String givenMobileNumber = "45916225";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(NO, "no-sparebankensor-bankid", wireMockFilePath)
                        .addAnotherWireMockFile(encapMockFilePath)
                        .addCredentialField(USERNAME.getFieldKey(), givenUserName)
                        .addCredentialField(MOBILENUMBER.getFieldKey(), givenMobileNumber)
                        .addCallbackData("activationCode", "25818567")
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .withHttpDebugTrace()
                        .withRequestFlagCreate(true)
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
