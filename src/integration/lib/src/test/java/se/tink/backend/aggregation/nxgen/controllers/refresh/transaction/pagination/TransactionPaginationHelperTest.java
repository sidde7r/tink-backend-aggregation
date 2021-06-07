package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper.SAFETY_THRESHOLD_NUMBER_OF_DAYS;
import static se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper.SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.AccountTransactionRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.TransactionRefreshScope;

@RunWith(MockitoJUnitRunner.class)
public class TransactionPaginationHelperTest {

    @Mock private Account account;

    @Test
    public void ensure_shouldFetchNextPage_returnsFalse_whenTransactionRefreshLimitIsReached() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionRefreshScope transactionRefreshScope = new TransactionRefreshScope();
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        transactionRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        refreshScope.setTransactions(transactionRefreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        List<AggregationTransaction> transactions = new ArrayList<>();

        AggregationTransaction transaction = mock(AggregationTransaction.class);
        Date transactionDate =
                Date.from(expectedLocalDate.atStartOfDay(ZoneId.of("UTC")).toInstant());
        when(transaction.getDate()).thenReturn(transactionDate);
        transactions.add(transaction);

        // create transactions before certain date to ensure number of overlaps
        for (int i = 0; i < SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS - 1; i++) {
            transaction = mock(AggregationTransaction.class);
            transactionDate =
                    Date.from(
                            expectedLocalDate
                                    .minusDays(1)
                                    .atStartOfDay(ZoneId.of("UTC"))
                                    .toInstant());
            when(transaction.getDate()).thenReturn(transactionDate);
            transactions.add(transaction);
        }

        // add transaction to ensure number of days overlap
        transaction = mock(AggregationTransaction.class);
        transactionDate =
                Date.from(
                        expectedLocalDate
                                .minusDays(SAFETY_THRESHOLD_NUMBER_OF_DAYS)
                                .atStartOfDay(ZoneId.of("UTC"))
                                .toInstant());
        when(transaction.getDate()).thenReturn(transactionDate);
        transactions.add(transaction);

        // when
        boolean contentWithRefresh = helper.shouldFetchNextPage(account, transactions);

        // then
        assertFalse(contentWithRefresh);
    }

    @Test
    public void
            ensure_shouldFetchNextPage_returnsTrue_whenTransactionRefreshLimitIsReachedButNoSafetyThresholdNumberOfDaysEnsured() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionRefreshScope transactionRefreshScope = new TransactionRefreshScope();
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        transactionRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        refreshScope.setTransactions(transactionRefreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        List<AggregationTransaction> transactions = new ArrayList<>();

        AggregationTransaction transaction = mock(AggregationTransaction.class);
        Date transactionDate =
                Date.from(expectedLocalDate.atStartOfDay(ZoneId.of("UTC")).toInstant());
        when(transaction.getDate()).thenReturn(transactionDate);
        transactions.add(transaction);

        // create transactions before certain date to ensure number of overlaps
        for (int i = 0; i < SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS - 1; i++) {
            transaction = mock(AggregationTransaction.class);
            transactionDate =
                    Date.from(
                            expectedLocalDate
                                    .minusDays(1)
                                    .atStartOfDay(ZoneId.of("UTC"))
                                    .toInstant());
            when(transaction.getDate()).thenReturn(transactionDate);
            transactions.add(transaction);
        }

        // add transaction which does not ensure number of days overlap
        transaction = mock(AggregationTransaction.class);
        transactionDate =
                Date.from(
                        expectedLocalDate
                                .minusDays(SAFETY_THRESHOLD_NUMBER_OF_DAYS - 1)
                                .atStartOfDay(ZoneId.of("UTC"))
                                .toInstant());
        when(transaction.getDate()).thenReturn(transactionDate);
        transactions.add(transaction);

        // when
        boolean contentWithRefresh = helper.shouldFetchNextPage(account, transactions);

        // then
        assertTrue(contentWithRefresh);
    }

    @Test
    public void
            ensure_shouldFetchNextPage_returnsTrue_whenTransactionRefreshLimitIsReachedButNoSafetyThresholdNumberOfOverlapsEnsured() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionRefreshScope transactionRefreshScope = new TransactionRefreshScope();
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        transactionRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        refreshScope.setTransactions(transactionRefreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        List<AggregationTransaction> transactions = new ArrayList<>();

        AggregationTransaction transaction = mock(AggregationTransaction.class);
        Date transactionDate =
                Date.from(expectedLocalDate.atStartOfDay(ZoneId.of("UTC")).toInstant());
        when(transaction.getDate()).thenReturn(transactionDate);
        transactions.add(transaction);

        // create SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS - 2 transactions before certain date to almost
        // ensure number of overlaps (edge case)
        for (int i = 0; i < SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS - 2; i++) {
            transaction = mock(AggregationTransaction.class);
            transactionDate =
                    Date.from(
                            expectedLocalDate
                                    .minusDays(1)
                                    .atStartOfDay(ZoneId.of("UTC"))
                                    .toInstant());
            when(transaction.getDate()).thenReturn(transactionDate);
            transactions.add(transaction);
        }

        // add transaction which ensures number of days overlap
        transaction = mock(AggregationTransaction.class);
        transactionDate =
                Date.from(
                        expectedLocalDate
                                .minusDays(SAFETY_THRESHOLD_NUMBER_OF_DAYS)
                                .atStartOfDay(ZoneId.of("UTC"))
                                .toInstant());
        when(transaction.getDate()).thenReturn(transactionDate);
        transactions.add(transaction);

        // when
        boolean contentWithRefresh = helper.shouldFetchNextPage(account, transactions);

        // then
        assertTrue(contentWithRefresh);
    }

    @Test
    public void
            ensure_getTransactionDateLimit_returnsProperRefreshDate_whenIsDefinedOnTransactionScope() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionRefreshScope transactionRefreshScope = new TransactionRefreshScope();
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        transactionRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        refreshScope.setTransactions(transactionRefreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertTrue(date.isPresent());
        assertEquals(
                expectedLocalDate.atStartOfDay().toInstant(ZoneOffset.UTC), date.get().toInstant());
    }

    @Test
    public void
            ensure_getTransactionDateLimit_returnsProperRefreshDate_whenIsDefinedOnAccountTransactionScope() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionRefreshScope transactionRefreshScope = new TransactionRefreshScope();
        refreshScope.setTransactions(transactionRefreshScope);
        AccountTransactionRefreshScope accountTransactionRefreshScope =
                new AccountTransactionRefreshScope();
        String accountIdentifier = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        accountTransactionRefreshScope.setAccountIdentifiers(
                Collections.singleton(accountIdentifier));
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        accountTransactionRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        transactionRefreshScope.setAccounts(Collections.singleton(accountTransactionRefreshScope));
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        when(account.getIdentifiers())
                .thenReturn(
                        Collections.singletonList(
                                AccountIdentifier.createOrThrow(accountIdentifier)));

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertTrue(date.isPresent());
        assertEquals(
                expectedLocalDate.atStartOfDay().toInstant(ZoneOffset.UTC), date.get().toInstant());
    }

    @Test
    public void
            ensure_getTransactionDateLimit_returnsProperRefreshDate_whenIsDefinedOnBothTransactionAndAccountTransactionScope() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionRefreshScope transactionRefreshScope = new TransactionRefreshScope();
        transactionRefreshScope.setTransactionBookedDateGte(LocalDate.parse("2021-06-03"));
        refreshScope.setTransactions(transactionRefreshScope);
        AccountTransactionRefreshScope accountTransactionRefreshScope =
                new AccountTransactionRefreshScope();
        String accountIdentifier = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        accountTransactionRefreshScope.setAccountIdentifiers(
                Collections.singleton(accountIdentifier));
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        accountTransactionRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        transactionRefreshScope.setAccounts(Collections.singleton(accountTransactionRefreshScope));
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        when(account.getIdentifiers())
                .thenReturn(
                        Collections.singletonList(
                                AccountIdentifier.createOrThrow(accountIdentifier)));

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertTrue(date.isPresent());
        assertEquals(
                expectedLocalDate.atStartOfDay().toInstant(ZoneOffset.UTC), date.get().toInstant());
    }

    @Test
    public void ensure_getTransactionDateLimit_returnsEmptyRefreshDate_whenRefreshScopeIsNull() {
        // given
        TransactionPaginationHelper helper = new TransactionPaginationHelper((RefreshScope) null);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertFalse(date.isPresent());
    }

    @Test
    public void
            ensure_getTransactionDateLimit_returnsEmptyRefreshDate_whenTransactionRefreshScopeIsNull() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertFalse(date.isPresent());
    }
}
