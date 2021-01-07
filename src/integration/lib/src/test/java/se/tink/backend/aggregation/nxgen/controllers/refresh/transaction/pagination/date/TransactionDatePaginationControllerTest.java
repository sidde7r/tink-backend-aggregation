package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
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

    private final int MAX_CONSECUTIVE_EMPTY_PAGES = 4;
    private final int DAYS_TO_FETCH = 89;

    @Mock private TransactionDatePaginator<Account> paginator;
    @Mock private Account account;

    private TransactionDatePaginationController<Account> paginationController;

    @Before
    public void setup() {
        paginationController =
                new TransactionDatePaginationController.Builder<>(paginator)
                        .setConsecutiveEmptyPagesLimit(MAX_CONSECUTIVE_EMPTY_PAGES)
                        .setAmountAndUnitToFetch(DAYS_TO_FETCH, ChronoUnit.DAYS)
                        .build();
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenTransactionDatePaginator_isNull() {
        new TransactionDatePaginationController.Builder<>(null);
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

    @Test
    public void ensureWeFetchWithExpectedPeriods() {
        List<Pair<LocalDateTime, LocalDateTime>> periods = Lists.newArrayList();
        when(paginator.getTransactionsFor(any(Account.class), any(Date.class), any(Date.class)))
                .then(
                        call -> {
                            periods.add(
                                    Pair.of(
                                            convertToDateTime(call.getArgument(1)),
                                            convertToDateTime(call.getArgument(2))));
                            return PaginatorResponseImpl.createEmpty();
                        });

        for (int i = 1; i <= MAX_CONSECUTIVE_EMPTY_PAGES; i++) {
            PaginatorResponse response = paginationController.fetchTransactionsFor(account);
            Assert.assertTrue(response.getTinkTransactions().isEmpty());
            boolean shouldBeAbleToFetchMore = i < MAX_CONSECUTIVE_EMPTY_PAGES;
            Assert.assertEquals(shouldBeAbleToFetchMore, response.canFetchMore().get());
        }

        Assert.assertEquals(MAX_CONSECUTIVE_EMPTY_PAGES, periods.size());

        for (int i = 0; i < periods.size(); i++) {
            Pair<LocalDateTime, LocalDateTime> period = periods.get(i);
            Assert.assertEquals(
                    period.getLeft(),
                    period.getRight().minusDays(DAYS_TO_FETCH).with(LocalTime.MIN));
            if (i > 0) {
                Pair<LocalDateTime, LocalDateTime> nextPeriod = periods.get(i - 1);
                Assert.assertEquals(
                        period.getRight().plus(1, ChronoUnit.MILLIS), nextPeriod.getLeft());
            }
        }
    }

    private LocalDateTime convertToDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.of("CET")).toLocalDateTime();
    }
}
