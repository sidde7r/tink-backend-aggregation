package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class GetTransactionsResponseTest {

    @Test
    public void getTinkTransactionsShouldReturnEmptyListIfTransactionsNull() {
        // given
        GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse(null, false);
        // when
        Collection<? extends Transaction> result = getTransactionsResponse.getTinkTransactions();
        // then
        assertThat(result).isEmpty();
    }
}
