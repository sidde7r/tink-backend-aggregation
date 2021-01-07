package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import static org.mockito.ArgumentMatchers.any;
import static se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController.MAX_TOTAL_EMPTY_PAGES;

import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class TransactionMonthPaginationControllerTest {
    private TransactionMonthPaginator paginator;
    private TransactionMonthPaginationController<Account> paginationController;

    private final Account account = Mockito.mock(Account.class);

    @Before
    public void setup() {
        paginator = Mockito.mock(TransactionMonthPaginator.class);
        paginationController =
                new TransactionMonthPaginationController<>(
                        paginator, ZoneId.of("Europe/Stockholm"));
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenTransactionDatePaginator_isNull() {
        new TransactionDatePaginationController.Builder<>(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenAccount_isNull() {
        paginationController.fetchTransactionsFor(null);
    }

    @Test
    public void ensureWeStopFetchingMoreTransactions_whenMaxTotalEmptyPages_isReached() {
        Mockito.when(
                        paginator.getTransactionsFor(
                                any(Account.class), any(Year.class), any(Month.class)))
                .thenReturn(PaginatorResponseImpl.createEmpty());

        paginationController.resetState();

        for (int i = 1; i <= MAX_TOTAL_EMPTY_PAGES; i++) {
            PaginatorResponse response = paginationController.fetchTransactionsFor(account);
            Assert.assertTrue(response.getTinkTransactions().isEmpty());
            boolean shouldBeAbleToFetchMore = i < MAX_TOTAL_EMPTY_PAGES;
            Assert.assertEquals(shouldBeAbleToFetchMore, response.canFetchMore().get());
        }

        Mockito.verify(paginator, Mockito.times(MAX_TOTAL_EMPTY_PAGES))
                .getTransactionsFor(any(Account.class), any(Year.class), any(Month.class));
        Assert.assertFalse(paginationController.fetchTransactionsFor(account).canFetchMore().get());
    }
}
