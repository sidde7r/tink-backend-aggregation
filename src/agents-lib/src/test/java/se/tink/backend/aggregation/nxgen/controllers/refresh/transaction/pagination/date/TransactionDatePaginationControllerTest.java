package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController.MAX_CONSECUTIVE_EMPTY_PAGES;

@RunWith(MockitoJUnitRunner.class)
public class TransactionDatePaginationControllerTest {

    @Mock
    private TransactionDatePaginator<Account> paginator;
    @Mock
    private Account account;

    private TransactionDatePaginationController<Account> paginationController;

    @Before
    public void setup() {
        paginationController = new TransactionDatePaginationController<>(paginator);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenTransactionDatePaginator_isNull() {
        new TransactionDatePaginationController<>(null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenAccount_isNull() {
        paginationController.fetchTransactionsFor(null);
    }

    @Test
    public void ensureNullTransactionsCollection_isConvertedToEmptyCollection() {
        when(paginator.getTransactionsFor(any(Account.class), any(Date.class), any(Date.class)))
                .thenReturn(null);

        Collection<? extends Transaction> transactions = paginationController.fetchTransactionsFor(account);
        Assert.assertTrue(transactions.isEmpty());
    }

    @Test
    public void ensureWeStopFetchingMoreTransactions_whenMaxConsecutiveEmptyPages_isReached() {
        when(paginator.getTransactionsFor(any(Account.class), any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        for (int i = 1; i <= MAX_CONSECUTIVE_EMPTY_PAGES; i++) {
            Assert.assertTrue(paginationController.fetchTransactionsFor(account).isEmpty());
            boolean shouldBeAbleToFetchMore = i < MAX_CONSECUTIVE_EMPTY_PAGES;
            Assert.assertEquals(shouldBeAbleToFetchMore, paginationController.canFetchMoreFor(account));
        }

        verify(paginator, times(MAX_CONSECUTIVE_EMPTY_PAGES))
                .getTransactionsFor(any(Account.class), any(Date.class), any(Date.class));
        Assert.assertFalse(paginationController.canFetchMoreFor(account));
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenTryingToFetchMoreTransactions_andCanFetchMore_returnFalse() {
        when(paginator.getTransactionsFor(any(Account.class), any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        for (int i = 1; i <= MAX_CONSECUTIVE_EMPTY_PAGES + 1; i++) {
            Assert.assertTrue(paginationController.fetchTransactionsFor(account).isEmpty());
        }
    }
}
