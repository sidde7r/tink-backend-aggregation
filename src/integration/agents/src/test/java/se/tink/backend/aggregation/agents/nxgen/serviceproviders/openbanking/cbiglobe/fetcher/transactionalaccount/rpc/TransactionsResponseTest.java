package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsResponseTest {

    @Test
    public void getTinkTransactionsShouldReturnEmptyListIfTransactionsNull() {
        // given
        TransactionsResponse transactionsResponse = new TransactionsResponse(null, false);
        // when
        Collection<? extends Transaction> result = transactionsResponse.getTinkTransactions();
        // then
        assertThat(result).isEmpty();
    }
}
