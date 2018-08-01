package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.core.account.Account;

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
    public void ensureNextKey_isForwarded_toNextFetchTransactionsRequest() {
        final String key1 = "key1";
        final String key2 = "key2";

        Mockito.when(paginatorResponse.getTinkTransactions()).thenReturn(Collections.emptyList());
        Mockito.when(paginatorResponse.canFetchMore()).thenReturn(Optional.of(true));
        Mockito.when(paginatorResponse.nextKey()).thenReturn(key1);
        Mockito.when(paginator.getTransactionsFor(account, null)).thenReturn(paginatorResponse);

        InOrder executionOrder = Mockito.inOrder(paginator);

        paginationController.fetchTransactionsFor(account);
        executionOrder.verify(paginator).getTransactionsFor(account, null);

        Mockito.when(paginatorResponse.canFetchMore()).thenReturn(Optional.of(true));
        Mockito.when(paginatorResponse.nextKey()).thenReturn(key2);
        Mockito.when(paginator.getTransactionsFor(account, key1)).thenReturn(paginatorResponse);

        paginationController.fetchTransactionsFor(account);
        executionOrder.verify(paginator).getTransactionsFor(account, key1);

        Mockito.when(paginatorResponse.canFetchMore()).thenReturn(Optional.of(true));
        Mockito.when(paginatorResponse.nextKey()).thenReturn(null);
        Mockito.when(paginator.getTransactionsFor(account, key2)).thenReturn(paginatorResponse);

        paginationController.fetchTransactionsFor(account);
        executionOrder.verify(paginator).getTransactionsFor(account, key2);

        executionOrder.verifyNoMoreInteractions();
    }
}
