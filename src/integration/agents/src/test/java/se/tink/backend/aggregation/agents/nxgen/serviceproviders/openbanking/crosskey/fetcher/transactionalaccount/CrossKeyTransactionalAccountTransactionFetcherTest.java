package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrossKeyTestUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@RunWith(MockitoJUnitRunner.class)
public class CrossKeyTransactionalAccountTransactionFetcherTest {
    @Mock private CrosskeyBaseApiClient apiClient;

    private CrossKeyTransactionalAccountTransactionFetcher
            crossKeyTransactionalAccountTransactionFetcher;

    @Before
    public void init() {
        crossKeyTransactionalAccountTransactionFetcher =
                new CrossKeyTransactionalAccountTransactionFetcher(apiClient);
    }

    @Test
    public void shouldFetchTransactionalAccountTransactions() {
        // given
        TransactionalAccount account = Mockito.mock(TransactionalAccount.class);

        when(apiClient.fetchTransactionalAccountTransactions(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(preparePaginatorResponse());
        // when
        PaginatorResponse transactions =
                crossKeyTransactionalAccountTransactionFetcher.getTransactionsFor(
                        account, CrossKeyTestUtils.PAGING_FROM, CrossKeyTestUtils.PAGING_TO);
        List<Transaction> tinkTransactions = new ArrayList(transactions.getTinkTransactions());

        // then
        assertEquals(2, tinkTransactions.size());
        Transaction transaction1 = tinkTransactions.get(0);

        assertEquals(TransactionTypes.DEFAULT, transaction1.getType());
        assertEquals("SUPERMARKET", transaction1.getDescription());
        assertEquals(-1443.01, transaction1.getExactAmount().getDoubleValue(), 0.001);
        assertEquals("EUR", transaction1.getExactAmount().getCurrencyCode());
        assertFalse(transaction1.isPending());
    }

    private CrosskeyTransactionsResponse preparePaginatorResponse() {
        return CrossKeyTestUtils.loadResourceFileContent(
                "checkingTransactions.json", CrosskeyTransactionsResponse.class);
    }
}
