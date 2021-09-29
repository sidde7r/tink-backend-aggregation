package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.wiremock;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

@Ignore
public class SebSEBusinessAgentWiremockTest {
    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/business/seb/wiremock/resources/configuration.yml";

    @Test
    public void testRefresh() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/business/seb/wiremock/resources/se_seb_business_ais.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/business/seb/wiremock/resources/agent-contract.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.SE)
                        .withProviderName("se-seb-business-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.CHECKING_TRANSACTIONS)
                        .addRefreshableItems(RefreshableItem.SAVING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.SAVING_TRANSACTIONS)
                        .addCredentialField("psu-corporate-id", "111111-1111")
                        .addCredentialField("SSN", "111111111112")
                        .enableDataDumpForContractFile()
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
