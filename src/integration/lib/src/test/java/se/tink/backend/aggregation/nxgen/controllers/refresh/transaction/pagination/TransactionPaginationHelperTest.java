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
import se.tink.libraries.credentials.service.AccountTransactionsRefreshScope;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.TransactionsRefreshScope;

@RunWith(MockitoJUnitRunner.class)
public class TransactionPaginationHelperTest {

    @Mock private CredentialsRequest request;
    @Mock private Account account;

    @Test
    public void ensure_shouldFetchNextPage_returnsFalse_whenTransactionRefreshLimitIsReached() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        transactionsRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        refreshScope.setTransactions(transactionsRefreshScope);
        when(request.getRefreshScope()).thenReturn(refreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(request);

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
        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        transactionsRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        refreshScope.setTransactions(transactionsRefreshScope);
        when(request.getRefreshScope()).thenReturn(refreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(request);

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
        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        transactionsRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        refreshScope.setTransactions(transactionsRefreshScope);
        when(request.getRefreshScope()).thenReturn(refreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(request);

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
            ensure_getTransactionDateLimit_returnsProperRefreshDate_whenIsDefinedOnTransactionsScope() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        transactionsRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        refreshScope.setTransactions(transactionsRefreshScope);
        when(request.getRefreshScope()).thenReturn(refreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(request);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertTrue(date.isPresent());
        assertEquals(
                expectedLocalDate.atStartOfDay().toInstant(ZoneOffset.UTC), date.get().toInstant());
    }

    @Test
    public void
            ensure_getTransactionDateLimit_returnsProperRefreshDate_whenIsDefinedOnAccountTransactionsScope() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        refreshScope.setTransactions(transactionsRefreshScope);
        AccountTransactionsRefreshScope accountTransactionsRefreshScope =
                new AccountTransactionsRefreshScope();
        String accountIdentifier = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        accountTransactionsRefreshScope.setAccountIdentifiers(
                Collections.singleton(accountIdentifier));
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        accountTransactionsRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        transactionsRefreshScope.setAccounts(
                Collections.singleton(accountTransactionsRefreshScope));
        when(request.getRefreshScope()).thenReturn(refreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(request);

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
            ensure_getTransactionDateLimit_returnsProperRefreshDate_whenIsDefinedOnBothTransactionsAndAccountTransactionsScope() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        transactionsRefreshScope.setTransactionBookedDateGte(LocalDate.parse("2021-06-03"));
        refreshScope.setTransactions(transactionsRefreshScope);
        AccountTransactionsRefreshScope accountTransactionsRefreshScope =
                new AccountTransactionsRefreshScope();
        String accountIdentifier = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        accountTransactionsRefreshScope.setAccountIdentifiers(
                Collections.singleton(accountIdentifier));
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        accountTransactionsRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        transactionsRefreshScope.setAccounts(
                Collections.singleton(accountTransactionsRefreshScope));
        when(request.getRefreshScope()).thenReturn(refreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(request);

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
        when(request.getRefreshScope()).thenReturn(null);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(request);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertFalse(date.isPresent());
    }

    @Test
    public void
            ensure_getTransactionDateLimit_returnsEmptyRefreshDate_whenTransactionsRefreshScopeIsNull() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        when(request.getRefreshScope()).thenReturn(refreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(request);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertFalse(date.isPresent());
    }
}
