package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationMockServerTest;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.models.Transaction;

public final class AmericanExpressV62UKMockServerAgentTest extends AgentIntegrationMockServerTest {

    private String USERNAME = "testUser";
    private String PASSWORD = "testPassword";

    @Test
    public void testLogin() throws Exception {

        prepareMockServer(
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62",
                "resources/mock.txt",
                "https://global.americanexpress.com");

        NewAgentTestContext context =
                new AgentIntegrationTest.Builder("uk", "uk-americanexpress-password")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .useWireMock(getWireMockPort())
                        .build()
                        .testRefresh();

        List<Account> accounts = context.getUpdatedAccounts();
        List<Transaction> transactions = context.getTransactions();

        Assert.assertEquals(accounts.size(), 1);
        Assert.assertEquals(transactions.size(), 4);
    }
}
