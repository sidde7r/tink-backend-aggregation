package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.account.enums.AccountExclusion;
import se.tink.libraries.amount.ExactCurrencyAmount;

public final class AmexV62UkMockServerAgentTest {

    private final String USERNAME = "testUser";
    private final String PASSWORD = "testPassword";

    private Account createAccount(
            String accountNumber,
            double availableCredit,
            double balance,
            String currencyCode,
            String bankId,
            String name,
            AccountTypes accountType,
            String holderName) {

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountExclusion(AccountExclusion.NONE);
        account.setExactAvailableCredit(
                new ExactCurrencyAmount(BigDecimal.valueOf(availableCredit), currencyCode));
        account.setExactBalance(new ExactCurrencyAmount(BigDecimal.valueOf(balance), currencyCode));
        account.setCurrencyCode(currencyCode);
        account.setBankId(bankId);
        account.setExcluded(false);
        account.setFavored(false);
        account.setName(name);
        account.setOwnership(1.0);
        account.setType(accountType);
        account.setUserModifiedExcluded(false);
        account.setUserModifiedName(false);
        account.setUserModifiedType(false);
        account.setIdentifiers(new ArrayList<>());
        account.setClosed(false);
        account.setHolderName(holderName);
        account.setFlags(new ArrayList<>());
        account.setAvailableCredit(availableCredit);
        account.setBalance(balance);
        return account;
    }

    private Transaction createTransaction(
            String accountId,
            double amount,
            long date,
            String description,
            boolean pending,
            boolean upcoming,
            TransactionTypes type) {

        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setDate(new Date(date));
        transaction.setDescription(description);
        transaction.setPending(pending);
        transaction.setType(type);
        transaction.setUpcoming(upcoming);
        return transaction;
    }

    @Test
    public void testRefresh() throws Exception {

        // Given
        WireMockTestServer server = new WireMockTestServer();
        server.prepareMockServer(
                new AapFileParser(
                        new ResourceFileReader()
                                .read(
                                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62/resources/amex-refresh-traffic.aap")));

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

        List<Account> expectedAccounts = Collections.singletonList(account);

        Transaction transaction1 =
                createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        -403.25,
                        1552561200000L,
                        "interesting transaction",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        Transaction transaction2 =
                createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        -64.84,
                        1553770800000L,
                        "good transaction",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        Transaction transaction3 =
                createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        64.84,
                        1554285600000L,
                        "PAYMENT RECEIVED - THANK YOU",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        Transaction transaction4 =
                createTransaction(
                        "fabc330b9f804c7ca6c2a21c3fd255ae",
                        551.4,
                        1553511600000L,
                        "PAYMENT RECEIVED - THANK YOU",
                        false,
                        false,
                        TransactionTypes.DEFAULT);

        List<Transaction> expectedTransactions =
                Arrays.asList(transaction1, transaction2, transaction3, transaction4);

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
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedAccounts, givenAccounts));
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedTransactions, givenTransactions));
    }
}
