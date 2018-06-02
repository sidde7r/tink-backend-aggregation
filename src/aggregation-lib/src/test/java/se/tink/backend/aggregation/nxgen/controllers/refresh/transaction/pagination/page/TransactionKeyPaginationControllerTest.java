package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.system.rpc.Transaction;

@SuppressWarnings("unchecked")
public class TransactionKeyPaginationControllerTest {
    private TransactionKeyPaginationController paginationController;
    private TransactionKeyPaginator<Account, String> paginator;
    private TransactionKeyPaginatorResponse<String> paginatorResponse;

    private final Account account = Mockito.mock(Account.class);

    @Before
    public void setup() {
        paginator = Mockito.mock(TransactionKeyPaginator.class);
        paginationController = new TransactionKeyPaginationController<>(paginator);
        paginatorResponse = Mockito.mock(TransactionKeyPaginatorResponse.class);
    }

    /**
     * Instantiation test
     */
    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenTransactionKeyPaginator_isNull() {
        new TransactionKeyPaginationController<>(null);
    }

    @Test
    public void ensureEmptyCollectionIsReturned_whenTransactionKeyPaginatorResponse_isNull() {
        Mockito.when(paginator.getTransactionsFor(Mockito.any(Account.class), Mockito.anyString()))
                .thenReturn(null);

        Collection<Transaction> transactions = paginationController.fetchTransactionsFor(account);
        Assert.assertTrue(transactions.isEmpty());
    }

    @Test
    public void ensureEmptyCollectionIsReturned_whenTransactionsCollection_isNull() {
        Mockito.when(paginatorResponse.getTinkTransactions()).thenReturn(null);
        Mockito.when(paginator.getTransactionsFor(Mockito.any(Account.class), Mockito.anyString()))
                .thenReturn(paginatorResponse);

        Collection<Transaction> transactions = paginationController.fetchTransactionsFor(account);
        Assert.assertTrue(transactions.isEmpty());
    }

    @Test
    public void ensureNextKey_isForwarded_toNextFetchTransactionsRequest() {
        final String key1 = "key1";
        final String key2 = "key2";

        Mockito.when(paginatorResponse.getTinkTransactions()).thenReturn(Collections.emptyList());
        Mockito.when(paginatorResponse.hasNext()).thenReturn(true);
        Mockito.when(paginatorResponse.nextKey()).thenReturn(key1);
        Mockito.when(paginator.getTransactionsFor(account, null)).thenReturn(paginatorResponse);

        InOrder executionOrder = Mockito.inOrder(paginator);

        paginationController.fetchTransactionsFor(account);
        Assert.assertTrue(paginationController.canFetchMoreFor(account));
        executionOrder.verify(paginator).getTransactionsFor(account, null);

        Mockito.when(paginatorResponse.hasNext()).thenReturn(true);
        Mockito.when(paginatorResponse.nextKey()).thenReturn(key2);
        Mockito.when(paginator.getTransactionsFor(account, key1)).thenReturn(paginatorResponse);

        paginationController.fetchTransactionsFor(account);
        Assert.assertTrue(paginationController.canFetchMoreFor(account));
        executionOrder.verify(paginator).getTransactionsFor(account, key1);

        Mockito.when(paginatorResponse.hasNext()).thenReturn(false);
        Mockito.when(paginatorResponse.nextKey()).thenReturn(null);
        Mockito.when(paginator.getTransactionsFor(account, key2)).thenReturn(paginatorResponse);

        paginationController.fetchTransactionsFor(account);
        Assert.assertFalse(paginationController.canFetchMoreFor(account));
        executionOrder.verify(paginator).getTransactionsFor(account, key2);

        executionOrder.verifyNoMoreInteractions();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenTryingToFetchMoreTransactions_andCanFetchMore_returnFalse() {
        Mockito.when(paginatorResponse.getTinkTransactions()).thenReturn(Collections.emptyList());
        Mockito.when(paginatorResponse.hasNext()).thenReturn(false);
        Mockito.when(paginatorResponse.nextKey()).thenReturn("key");
        Mockito.when(paginator.getTransactionsFor(Mockito.any(Account.class), Mockito.anyString()))
                .thenReturn(paginatorResponse);

        paginationController.fetchTransactionsFor(account);
        Assert.assertFalse(paginationController.canFetchMoreFor(account));
        paginationController.fetchTransactionsFor(account);
    }
}
