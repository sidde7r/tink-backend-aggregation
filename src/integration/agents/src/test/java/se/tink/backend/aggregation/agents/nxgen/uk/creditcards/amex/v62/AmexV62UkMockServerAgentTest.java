package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import static se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts.compareAccounts;
import static se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts.compareTransactions;
import static se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts.createAccount;
import static se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts.createTransaction;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.utils.wiremock.WiremockS3LogRequestResponseParser;
import se.tink.backend.aggregation.agents.framework.wiremock.AgentIntegrationMockServerTest;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;

public final class AmexV62UkMockServerAgentTest extends AgentIntegrationMockServerTest {

    private String USERNAME = "testUser";
    private String PASSWORD = "testPassword";

    @Test
    public void testRefresh() throws Exception {

        // Given
        prepareMockServer(
                new WiremockS3LogRequestResponseParser(
                        String.format(
                                "%s/%s",
                                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62",
                                "resources/mock.txt"),
                        "https://global.americanexpress.com"));

        Account account =
                createAccount(
                        "XXX-11111",
                        0,
                        0,
                        "GBP",
                        "XXX11111",
                        "American Express Basic Card - 11111",
                        AccountTypes.CREDIT_CARD,
                        "MR M LASTNAME");

        List<Account> expectedAccounts = Arrays.asList(new Account[] {account});

        Transaction transaction1 =
                createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        -403.25,
                        1552561200000l,
                        "interesting transaction",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        Transaction transaction2 =
                createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        -64.84,
                        1553770800000l,
                        "good transaction",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        Transaction transaction3 =
                createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        64.84,
                        1554285600000l,
                        "PAYMENT RECEIVED - THANK YOU",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        Transaction transaction4 =
                createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        551.4,
                        1553511600000l,
                        "PAYMENT RECEIVED - THANK YOU",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        List<Transaction> expectedTransactions =
                Arrays.asList(
                        new Transaction[] {transaction1, transaction2, transaction3, transaction4});

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
