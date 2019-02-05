package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionIndexPaginationControllerTest {

    @Mock
    private TransactionIndexPaginator<Account> paginator;
    @Mock
    private Account account;
    @Mock
    private Transaction transaction;

    private TransactionIndexPaginationController<Account> paginationController;

    @Before
    public void setup() {
        paginationController = new TransactionIndexPaginationController<>(paginator);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenTransactionIndexPaginator_isNull() {
        new TransactionIndexPaginationController<>(null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenAccount_isNull() {
        paginationController.fetchTransactionsFor(null);
    }

    @Test
    public void ensureStopFetching_whenNumberOfTransactionsFetched_isLess_thanNumberOfTransactionsToFetch() {
        Collection<Transaction> mockTransactions = new ArrayList<>();
        mockTransactions.add(transaction);

        when(paginator.getTransactionsFor(Mockito.any(Account.class), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(PaginatorResponseImpl.create(mockTransactions));

        paginationController.fetchTransactionsFor(account);

        verify(paginator, times(1))
                .getTransactionsFor(any(Account.class), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void ensureEmptyCollection_isReturned_andCanFetchMore_isFalse_whenListOfFetchedTransactionIsEmpty() {
        when(paginator.getTransactionsFor(Mockito.any(Account.class), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(PaginatorResponseImpl.createEmpty());

        PaginatorResponse response = paginationController.fetchTransactionsFor(account);

        Assert.assertTrue(response.getTinkTransactions().isEmpty());
        Assert.assertFalse(response.canFetchMore().get());
    }
}
