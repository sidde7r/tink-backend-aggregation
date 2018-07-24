package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import static org.mockito.Mockito.doReturn;
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
    public void setup(){
        paginationController = new TransactionIndexPaginationController<>(paginator);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenTransactionIndexPaginator_isNull(){
        new TransactionIndexPaginationController<>(null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenAccount_isNull(){
        paginationController.fetchTransactionsFor(null);
    }

    @Test
    public void ensureStopFetching_whenNumberOfTransactionsFetched_isLess_thanNumberOfTransactionsToFetch(){
        Collection<Transaction> mockTransactions = new ArrayList<>();
        mockTransactions.add(transaction);
        doReturn(mockTransactions).when(paginator).getTransactionsFor(Mockito.any(Account.class), Mockito.anyInt(),
                Mockito.anyInt());
        Assert.assertTrue(paginationController.canFetchMoreFor(account));
        paginationController.fetchTransactionsFor(account);
        Assert.assertFalse(paginationController.canFetchMoreFor(account));
    }

    @Test
    public void ensureEmptyCollection_isReturned_andCanFetchMore_isFalse_whenPaginatorReturnsNull(){
        when(paginator.getTransactionsFor(Mockito.any(Account.class), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(null);
        Assert.assertTrue(paginationController.canFetchMoreFor(account));
        Assert.assertTrue(paginationController.fetchTransactionsFor(account).isEmpty());
        Assert.assertFalse(paginationController.canFetchMoreFor(account));
    }

    // Edge case test when fetching 0 transactions
    @Test
    public void ensureEmptyCollection_isReturned_andCanFetchMore_isFalse_whenListOfFetchedTransactionIsEmpty(){
        when(paginator.getTransactionsFor(Mockito.any(Account.class), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Collections.emptyList());
        Assert.assertTrue(paginationController.canFetchMoreFor(account));
        Assert.assertTrue(paginationController.fetchTransactionsFor(account).isEmpty());
        Assert.assertFalse(paginationController.canFetchMoreFor(account));
    }
}
