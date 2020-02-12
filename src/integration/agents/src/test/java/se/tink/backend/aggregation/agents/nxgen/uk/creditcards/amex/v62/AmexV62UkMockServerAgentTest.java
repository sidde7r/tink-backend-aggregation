package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts;
import se.tink.backend.aggregation.agents.framework.wiremock.AgentIntegrationMockServerTest;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;

public final class AmexV62UkMockServerAgentTest extends AgentIntegrationMockServerTest {

    private final String USERNAME = "testUser";
    private final String PASSWORD = "testPassword";

    @Test
    public void testRefresh() throws Exception {

        // Given
        prepareMockServer(
                new AapFileParser(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62/resources/amex-refresh-traffic.aap",
                        "https://global.americanexpress.com"));

        Account account =
                AgentContractEntitiesAsserts.createAccount(
                        "XXX-11111",
                        0,
                        0,
                        "GBP",
                        "XXX11111",
                        "American Express Basic Card - 11111",
                        AccountTypes.CREDIT_CARD,
                        "MR M LASTNAME");

        List<Account> expectedAccounts = Collections.singletonList(account);

        Transaction transaction1 =
                AgentContractEntitiesAsserts.createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        -403.25,
                        1552561200000L,
                        "interesting transaction",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        Transaction transaction2 =
                AgentContractEntitiesAsserts.createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        -64.84,
                        1553770800000L,
                        "good transaction",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        Transaction transaction3 =
                AgentContractEntitiesAsserts.createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        64.84,
                        1554285600000L,
                        "PAYMENT RECEIVED - THANK YOU",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        Transaction transaction4 =
                AgentContractEntitiesAsserts.createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        551.4,
                        1553511600000L,
                        "PAYMENT RECEIVED - THANK YOU",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        List<Transaction> expectedTransactions =
                Arrays.asList(transaction1, transaction2, transaction3, transaction4);

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
        AgentContractEntitiesAsserts.compareAccounts(expectedAccounts, givenAccounts);
        AgentContractEntitiesAsserts.compareTransactions(expectedTransactions, givenTransactions);
    }
}
