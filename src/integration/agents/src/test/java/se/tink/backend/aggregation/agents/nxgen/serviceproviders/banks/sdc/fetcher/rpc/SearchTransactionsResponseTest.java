package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.parser.SdcSeTransactionParser;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SearchTransactionsResponseTest {
    @Test
    public void getTinkTransactions() throws Exception {
        SearchTransactionsResponse response = SearchTransactionsResponseTestData.getTestData();

        SdcSeTransactionParser transactionParser = new SdcSeTransactionParser();
        Collection<Transaction> transactions = response.getTinkTransactions(transactionParser);

        assertNotNull(transactions);
        assertTrue(transactions.size() > 0);
        for (Transaction transaction : transactions) {
            assertNotNull(transaction.getDescription());
            assertNotNull(transaction.getDate());
            assertTrue(transaction.getAmount().getValue() != 0);
        }
    }
    @Test
    public void getTinkCreditCardTransactions() throws Exception {
        SearchTransactionsResponse response = SearchCreditCardTransactionsResponseTestData.getTestData();

        CreditCardAccount creditCardAccount = CreditCardAccount.builder("uniqueIdentifier", Amount.inSEK(9500.0),
                Amount.inSEK(10500.0))
                .setAccountNumber("0123456789")
                .setName("Credit-card")
                .build();

        SdcSeTransactionParser transactionParser = new SdcSeTransactionParser();
        Collection<CreditCardTransaction> transactions = response.getTinkCreditCardTransactions(creditCardAccount, transactionParser);

        assertNotNull(transactions);
        assertTrue(transactions.size() > 0);
        assertEquals(1, transactions.stream().filter(Transaction::isPending).count());
        assertEquals(6, transactions.stream().filter(t->!t.isPending()).count());
        for (CreditCardTransaction transaction : transactions) {
            assertNotNull(transaction.getDescription());
            assertNotNull(transaction.getDate());
            assertTrue(transaction.getCreditAccount().isPresent());
            assertTrue(transaction.getAmount().getValue() != 0);
        }
    }
    @Test
    public void getTinkTransactionsWithReservations() throws Exception {
        SearchTransactionsResponse response = SearchTransactionsResponseTestData.getTestDataWithReservations();

        SdcSeTransactionParser transactionParser = new SdcSeTransactionParser();
        Collection<Transaction> transactions = response.getTinkTransactions(transactionParser);

        assertNotNull(transactions);
        assertTrue(transactions.size() > 0);
        assertEquals(1, transactions.stream().filter(Transaction::isPending).count());
        for (Transaction transaction : transactions) {
            assertNotNull(transaction.getDescription());
            assertNotNull(transaction.getDate());
            assertTrue(transaction.getAmount().getValue() != 0);
        }
    }
    @Test
    public void getTinkTransactionsEmptyResponse() throws Exception {
        SearchTransactionsResponse response = SearchTransactionsResponseTestData.getTestEmptyData();

        SdcSeTransactionParser transactionParser = new SdcSeTransactionParser();
        Collection<Transaction> transactions = response.getTinkTransactions(transactionParser);

        assertNotNull(transactions);
        assertTrue(transactions.size() == 0);
    }
}
