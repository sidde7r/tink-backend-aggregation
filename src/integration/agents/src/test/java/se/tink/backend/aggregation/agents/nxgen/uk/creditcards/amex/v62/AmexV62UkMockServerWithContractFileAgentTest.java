package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import java.util.List;
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
import se.tink.backend.aggregation.agents.framework.wiremock.utils.S3LogFormatAdapter;
import se.tink.backend.aggregation.agents.models.Transaction;

public class AmexV62UkMockServerWithContractFileAgentTest {

    private final String USERNAME = "testUser";
    private final String PASSWORD = "testPassword";

    @Test
    public void testRefreshWithJSONContractFile() throws Exception {
        // Given
        WireMockTestServer server = new WireMockTestServer();
        server.prepareMockServer(
                new AapFileParser(
                        new S3LogFormatAdapter()
                                .toMockFileFormat(
                                        new ResourceFileReader()
                                                .read(
                                                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62/resources/amex-refresh-traffic.s3"))));

        AgentContractEntitiesJsonFileParser contractParser =
                new AgentContractEntitiesJsonFileParser();
        AgentContractEntity expected =
                contractParser.parseContractOnBasisOfFile(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62/resources/agent-contract.json");

        List<Account> expectedAccounts = expected.getAccounts();
        List<Transaction> expectedTransactions = expected.getTransactions();

        final WireMockConfiguration configuration =
                WireMockConfiguration.builder("localhost:" + server.getHttpsPort()).build();

        // When
        NewAgentTestContext context =
                new AgentIntegrationTest.Builder("uk", "uk-americanexpress-password")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setWireMockConfiguration(configuration)
                        .build()
                        .testRefresh();

        List<Transaction> givenTransactions = context.getTransactions();
        List<Account> givenAccounts = context.getUpdatedAccounts();

        // Then
        AgentContractEntitiesAsserts.compareAccounts(expectedAccounts, givenAccounts);
        AgentContractEntitiesAsserts.compareTransactions(expectedTransactions, givenTransactions);
    }
}
