package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController.MAX_CONSECUTIVE_EMPTY_PAGES;

import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RunWith(MockitoJUnitRunner.class)
public class TransactionDatePaginationControllerTest {

    @Mock private TransactionDatePaginator<Account> paginator;
    @Mock private Account account;

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
    public void ensureWeStopFetchingMoreTransactions_whenMaxConsecutiveEmptyPages_isReached() {
        when(paginator.getTransactionsFor(any(Account.class), any(Date.class), any(Date.class)))
                .thenReturn(PaginatorResponseImpl.createEmpty());

        for (int i = 1; i <= MAX_CONSECUTIVE_EMPTY_PAGES; i++) {
            PaginatorResponse response = paginationController.fetchTransactionsFor(account);
            Assert.assertTrue(response.getTinkTransactions().isEmpty());
            boolean shouldBeAbleToFetchMore = i < MAX_CONSECUTIVE_EMPTY_PAGES;
            Assert.assertEquals(shouldBeAbleToFetchMore, response.canFetchMore().get());
        }

        verify(paginator, times(MAX_CONSECUTIVE_EMPTY_PAGES))
                .getTransactionsFor(any(Account.class), any(Date.class), any(Date.class));
        Assert.assertFalse(paginationController.fetchTransactionsFor(account).canFetchMore().get());
    }
}
