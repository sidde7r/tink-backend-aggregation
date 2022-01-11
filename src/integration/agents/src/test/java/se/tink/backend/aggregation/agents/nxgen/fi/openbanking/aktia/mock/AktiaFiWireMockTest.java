package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.mock;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class AktiaFiWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/aktia/mock/resources";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "/configuration.yml";

    private static final String WIREMOCK_FULL = RESOURCES_PATH + "/wireMock.aap";
    private static final String WIREMOCK_EMPTY_TRX_LIST =
            RESOURCES_PATH + "/wireMock_empty_trx_list.aap";
    private static final String WIREMOCK_MANY_DUPLICATES =
            RESOURCES_PATH + "/wireMock_many_duplicates.aap";

    private AgentsServiceConfiguration configuration;

    private AgentWireMockRefreshTest buildTest(String wiremockPath) {
        return AgentWireMockRefreshTest.nxBuilder()
                .withMarketCode(MarketCode.FI)
                .withProviderName("fi-aktia-ob")
                .withWireMockFilePath(wiremockPath)
                .withConfigFile(configuration)
                .testFullAuthentication()
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems()
                .addCallbackData("code", "dummyCode")
                .addCredentialField("username", "dummyUser")
                .addCredentialField("password", "dummyPassword")
                .build();
    }

    @Before
    public void setup() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    @Test
    public void testRefresh() throws Exception {

        // aap file has duplicated one transaction with transactionId
        // DE185C3F7F687846740E65F7CAD6C577F29155AF1ADA58EAA591580C80877426

        // given
        final String contractFilePath = RESOURCES_PATH + "/agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest = buildTest(WIREMOCK_FULL);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testRefreshWithEmptyTransactionList() throws Exception {

        // given
        final String contractFilePath = RESOURCES_PATH + "/agent-contract-empty-trx_list.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildTest(WIREMOCK_EMPTY_TRX_LIST);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testRefreshWithManyDuplicatesInTransactionList() throws Exception {

        /*aap file has many duplicated transactions with transactionId DE185C3F7F687846740E65F7CAD6C577F29155AF1ADA58EAA591580C80877426 (checking)
        and 069AE39835FD39BB9EB449919EEC8811079597B5692D1A3A1B4A96078A8A4782 (saving) + also duplicated transactions across accounts
        */

        // given
        final String contractFilePath = RESOURCES_PATH + "/agent-contract-many-duplicates.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildTest(WIREMOCK_MANY_DUPLICATES);

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
