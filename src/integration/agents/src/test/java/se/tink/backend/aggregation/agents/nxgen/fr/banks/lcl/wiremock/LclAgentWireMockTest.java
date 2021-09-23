package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.wiremock;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class LclAgentWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/lcl/wiremock/resources/";

    private AgentsServiceConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        configuration =
                AgentsServiceConfigurationReader.read(
                        new File(RESOURCES_PATH, "configuration.yml").getPath());
    }

    @Test
    public void shouldRefreshCheckingAccounts() throws Exception {

        // given
        AgentWireMockRefreshTest refreshTest =
                wireMockRefreshTest(
                        new File(RESOURCES_PATH, "refresh-with-multiple-checking-accounts.aap"));

        // and
        AgentContractEntity expected =
                expectedEntity(
                        new File(
                                RESOURCES_PATH,
                                "refresh-with-multiple-checking-accounts-contract.json"));

        // when
        refreshTest.executeRefresh();

        // then
        refreshTest.assertExpectedData(expected);
    }

    private AgentWireMockRefreshTest wireMockRefreshTest(File wireMockFile) {
        return AgentWireMockRefreshTest.nxBuilder()
                .withMarketCode(MarketCode.FR)
                .withProviderName("fr-lcl-password")
                .withWireMockFilePath(wireMockFile.getPath())
                .withConfigFile(configuration)
                .testFullAuthentication()
                .addRefreshableItems(
                        RefreshableItem.REFRESHABLE_ITEMS_ALL.toArray(new RefreshableItem[0]))
                .addCredentialField(Field.Key.USERNAME.getFieldKey(), "test-lcl-username")
                .addCredentialField(Field.Key.PASSWORD.getFieldKey(), "test-lcl-password")
                .build();
    }

    private AgentContractEntity expectedEntity(File contractFile) {
        return new AgentContractEntitiesJsonFileParser()
                .parseContractOnBasisOfFile(contractFile.getPath());
    }
}
