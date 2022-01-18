package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.mock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class CbiGlobeAgentSupportingAllPsd2AllAccountsWithOwnerNameWiremockTest {

    private static final String AAP_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe/mock/resources/test-refresh-supporting-all-psd2-all-accounts-with-owner-name.aap";

    private static String CONTRACT_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe/mock/resources/agent-contract-with-owners.json";

    private static String CONFIGURATION_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe/mock/resources/configuration.yml";

    @Test
    public void testRefresh() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE_PATH);

        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(MarketCode.IT, "it-credem-oauth2", AAP_PATH)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        // workaround to assign two values to one key: code = acc , code = trans
                        .addCallbackData("code", "acc&trans")
                        .addCallbackData("result", "success")
                        .withConfigurationFile(configuration)
                        .dumpContentForContractFile()
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(CONTRACT_FILE_PATH);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
