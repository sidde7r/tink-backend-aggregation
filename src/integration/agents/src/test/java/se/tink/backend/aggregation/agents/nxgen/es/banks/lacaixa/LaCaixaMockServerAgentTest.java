package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.identitydata.IdentityData;

public class LaCaixaMockServerAgentTest {

    private static final String USERNAME = "dummyUsername";
    private static final String PASSWORD = "dummyPassword";

    @Test
    public void testRefresh() throws Exception {

        // Given
        WireMockTestServer server = new WireMockTestServer();
        server.prepareMockServer(
                new AapFileParser(
                        new ResourceFileReader()
                                .read(
                                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/lacaixa/resources/caixa-refresh-traffic.aap")));

        final WireMockConfiguration configuration =
                WireMockConfiguration.builder("localhost:" + server.getHttpsPort()).build();

        AgentContractEntitiesJsonFileParser contractParser =
                new AgentContractEntitiesJsonFileParser();
        AgentContractEntity expected =
                contractParser.parseContractOnBasisOfFile(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/lacaixa/resources/agent-contract.json");

        List<Map<String, Object>> expectedAccounts = expected.getAccounts();
        List<Map<String, Object>> expectedTransactions = expected.getTransactions();
        Map<String, Object> expectedIdentityData = expected.getIdentityData();

        // When
        NewAgentTestContext context =
                new AgentIntegrationTest.Builder("es", "es-lacaixa-password")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setWireMockConfiguration(configuration)
                        .build()
                        .testRefresh();

        List<Transaction> givenTransactions = context.getTransactions();
        List<Account> givenAccounts = context.getUpdatedAccounts();
        IdentityData givenIdentityData = context.getIdentityData();

        // Then
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedAccounts, givenAccounts));
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedTransactions, givenTransactions));
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        Collections.singletonList(expectedIdentityData),
                        Collections.singletonList(givenIdentityData)));
    }
}
