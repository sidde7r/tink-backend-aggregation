package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class GetTransactionsResponseTest {

    @Test
    public void shouldReturnEmptyCollectionWhenNoTransactions() {
        // given
        GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse();

        // when
        Collection<? extends Transaction> resp = getTransactionsResponse.getTinkTransactions();

        // then
        assertNotNull(resp);
        assertEquals(0, resp.size());
    }
}
