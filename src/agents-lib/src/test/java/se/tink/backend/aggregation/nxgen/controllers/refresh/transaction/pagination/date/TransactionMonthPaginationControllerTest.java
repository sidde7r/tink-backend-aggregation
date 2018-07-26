package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import static org.mockito.ArgumentMatchers.any;
import static se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController.MAX_TOTAL_EMPTY_PAGES;

public class TransactionMonthPaginationControllerTest {
    private TransactionMonthPaginator paginator;
    private TransactionMonthPaginationController<Account> paginationController;

    private final Account account = Mockito.mock(Account.class);

    @Before
    public void setup() {
        paginator = Mockito.mock(TransactionMonthPaginator.class);
        paginationController = new TransactionMonthPaginationController<>(paginator, ZoneId.of("Europe/Stockholm"));
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
        Mockito.when(paginator.getTransactionsFor(any(Account.class), any(Year.class), any(Month.class)))
                .thenReturn(null);

        Collection<? extends Transaction> transactions = paginationController.fetchTransactionsFor(account);
        Assert.assertTrue(transactions.isEmpty());
    }

    @Test
    public void ensureWeStopFetchingMoreTransactions_whenMaxTotalEmptyPages_isReached() {
        Mockito.when(paginator.getTransactionsFor(any(Account.class), any(Year.class), any(Month.class)))
                .thenReturn(Collections.emptyList());

        for (int i = 1; i <= MAX_TOTAL_EMPTY_PAGES; i++) {
            Assert.assertTrue(paginationController.fetchTransactionsFor(account).isEmpty());
            boolean shouldBeAbleToFetchMore = i < MAX_TOTAL_EMPTY_PAGES;
            Assert.assertEquals(shouldBeAbleToFetchMore, paginationController.canFetchMoreFor(account));
        }

        Mockito.verify(paginator, Mockito.times(MAX_TOTAL_EMPTY_PAGES))
                .getTransactionsFor(any(Account.class), any(Year.class), any(Month.class));
        Assert.assertFalse(paginationController.canFetchMoreFor(account));
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenTryingToFetchMoreTransactions_andCanFetchMore_returnFalse() {
        Mockito.when(paginator.getTransactionsFor(any(Account.class), any(Year.class), any(Month.class)))
                .thenReturn(Collections.emptyList());

        for (int i = 1; i <= MAX_TOTAL_EMPTY_PAGES + 1; i++) {
            Assert.assertTrue(paginationController.fetchTransactionsFor(account).isEmpty());
        }
    }
}
