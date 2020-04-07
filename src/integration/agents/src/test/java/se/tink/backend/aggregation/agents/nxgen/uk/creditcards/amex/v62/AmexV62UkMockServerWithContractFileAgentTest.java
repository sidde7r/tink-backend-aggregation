package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

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

public class AmexV62UkMockServerWithContractFileAgentTest {

    private final String USERNAME = "testUser";
    private final String PASSWORD = "testPassword";

    @Test
    public void testRefreshWithJSONContractFile() throws Exception {
        // Given
        WireMockTestServer server = new WireMockTestServer();
        server.prepareMockServer(
                new AapFileParser(
                        new ResourceFileReader()
                                .read(
                                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62/resources/amex-refresh-traffic.aap")));

        AgentContractEntitiesJsonFileParser contractParser =
                new AgentContractEntitiesJsonFileParser();
        AgentContractEntity expected =
                contractParser.parseContractOnBasisOfFile(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62/resources/agent-contract.json");

        List<Map<String, Object>> expectedAccounts = expected.getAccounts();
        List<Map<String, Object>> expectedTransactions = expected.getTransactions();
        Map<String, Object> expectedIdentityData = expected.getIdentityData();

        final WireMockConfiguration configuration =
                WireMockConfiguration.builder("localhost:" + server.getHttpsPort()).build();

        // When
        NewAgentTestContext context =
                new AgentIntegrationTest.Builder("uk", "uk-americanexpress-password")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
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
