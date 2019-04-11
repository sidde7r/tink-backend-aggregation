package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class FetchTransactionsResponseTest {

    @Test
    public void fetchTransactionsFor() throws Exception {
        FetchTransactionsResponse fetchTransactionsResponse =
                FetchTransactionsResponseTestData.getTestData();
        Collection<Transaction> transactions = fetchTransactionsResponse.getTinkTransactions();

        for (Transaction transaction : transactions) {
            Assert.assertNotNull(transaction.getDate());
            Assert.assertTrue(transaction.getAmount().getValue() != 0);
        }
    }
}
