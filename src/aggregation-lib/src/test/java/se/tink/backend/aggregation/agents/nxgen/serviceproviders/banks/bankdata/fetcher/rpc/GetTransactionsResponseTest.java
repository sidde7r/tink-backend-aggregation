package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GetTransactionsResponseTest {
    @Test
    public void parseGetTransactionsResponse() throws Exception {
        GetTransactionsResponse getTransactionsResponse = GetTransactionsResponseTestData.getTestData();
        assertNotNull(getTransactionsResponse);

        Collection<Transaction> transactions = getTransactionsResponse.getTinkTransactions();
        for (Transaction transaction : transactions) {
            assertTrue(transaction.getAmount().getValue() == -10.0);
            assertNotNull(transaction.getDescription());
        }
    }
}