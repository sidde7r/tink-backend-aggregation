package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import static se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts.compareAccounts;
import static se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts.compareTransactions;

import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.wiremock.AgentIntegrationMockServerTest;
import se.tink.backend.aggregation.agents.models.Transaction;

public class AmexV62UkMockServerWithContractFileAgentTest extends AgentIntegrationMockServerTest {

    private String USERNAME = "testUser";
    private String PASSWORD = "testPassword";

    @Test
    public void testRefreshWithJSONContractFile() throws Exception {
        // Given
        prepareMockServer(
                new se.tink.backend.aggregation.agents.framework.utils.wiremock
                        .WiremockS3LogRequestResponseParser(
                        String.format(
                                "%s/%s",
                                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62",
                                "resources/mock.txt"),
                        "https://global.americanexpress.com"));

        AgentContractEntitiesJsonFileParser contractParser =
                new AgentContractEntitiesJsonFileParser();
        AgentContractEntity expected =
                contractParser.parseContractOnBasisOfFile(
                        String.format(
                                "%s/%s",
                                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62",
                                "resources/contract.json"));

        List<Account> expectedAccounts = expected.getAccounts();
        List<Transaction> expectedTransactions = expected.getTransactions();

        // When
        NewAgentTestContext context =
                new AgentIntegrationTest.Builder("uk", "uk-americanexpress-password")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .useWireMockServerHost("localhost:" + getWireMockPort())
                        .build()
                        .testRefresh();

        List<Transaction> givenTransactions = context.getTransactions();
        List<Account> givenAccounts = context.getUpdatedAccounts();

        // Then
        compareAccounts(expectedAccounts, givenAccounts);
        compareTransactions(expectedTransactions, givenTransactions);
    }
}
