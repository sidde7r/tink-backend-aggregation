package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.TestDataReader;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class GetTransactionsResponseTest {
    @Test
    public void parseGetTransactionsResponse() {
        GetTransactionsResponse getTransactionsResponse =
                TestDataReader.readFromFile(
                        TestDataReader.TRANSACTIONS_RESP, GetTransactionsResponse.class);
        assertNotNull(getTransactionsResponse);

        Collection<Transaction> transactions = getTransactionsResponse.getTinkTransactions();
        for (Transaction transaction : transactions) {
            assertThat(transaction.getExactAmount().getDoubleValue()).isEqualTo(-10.0);
            assertNotNull(transaction.getDescription());
        }
    }
}
