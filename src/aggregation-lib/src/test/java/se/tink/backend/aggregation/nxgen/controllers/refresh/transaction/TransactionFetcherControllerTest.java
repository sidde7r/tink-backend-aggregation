package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction;

import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.core.Amount;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionFetcherControllerTest {

    @Mock
    private TransactionPaginationHelper paginationHelper;
    @Mock
    private TransactionPaginator<TransactionalAccount> transactionalAccountPaginator;
    @Mock
    private TransactionPaginator<CreditCardAccount> creditCardAccountPaginator;
    @Mock
    private UpcomingTransactionFetcher upcomingTransactionFetcher;
    @Mock
    private TransactionalAccount account;

    private TransactionFetcherController<TransactionalAccount> fetcherController;

    @Before
    public void setup() {
        when(transactionalAccountPaginator.canFetchMoreFor(account)).thenReturn(false);
        when(upcomingTransactionFetcher.fetchUpcomingTransactionsFor(account)).thenReturn(Collections.emptyList());
        fetcherController = new TransactionFetcherController<>(
                paginationHelper, transactionalAccountPaginator, upcomingTransactionFetcher
        );
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenBothPaginationHelperAndPaginator_isNull() {
        new TransactionFetcherController<>(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenPaginationHelper_isNull() {
        new TransactionFetcherController<>(null, transactionalAccountPaginator);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenPaginator_isNull() {
        new TransactionFetcherController<>(paginationHelper, null);
    }

    @Test
    public void ensureExceptionIsNotThrown_whenUpcomingTransactionFetcher_isNull() {
        new TransactionFetcherController<>(paginationHelper, transactionalAccountPaginator, null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenAccount_isNull() {
        fetcherController.fetchTransactionsFor(null);
    }

    @Test
    public void ensureFetchTransactionsFor_isCalledUntil_canFetchMore_isFalse() {
        when(transactionalAccountPaginator.canFetchMoreFor(account)).thenReturn(true).thenReturn(false);
        // Never content with refresh
        when(paginationHelper.isContentWithRefresh(any(Account.class), anyList()))
                .thenReturn(false);

        fetcherController.fetchTransactionsFor(account);

        verify(transactionalAccountPaginator, times(2)).canFetchMoreFor(account);
    }

    @Test
    public void ensureFetchTransactionsFor_isCalledUntil_isContentWithRefresh_isTrue() {
        // Can always fetch more transactions
        when(transactionalAccountPaginator.canFetchMoreFor(account)).thenReturn(true);
        when(paginationHelper.isContentWithRefresh(any(Account.class), anyList()))
                .thenReturn(false).thenReturn(true);

        fetcherController.fetchTransactionsFor(account);

        verify(transactionalAccountPaginator, times(2)).canFetchMoreFor(account);
    }

    @Test
    public void ensureEmptyList_isReturned_ifPaginatorReturn_null() {
        when(transactionalAccountPaginator.fetchTransactionsFor(account)).thenReturn(null);

        Collection<AggregationTransaction> transactions = fetcherController.fetchTransactionsFor(account);

        Assert.assertTrue(transactions.isEmpty());
    }

    @Test
    public void ensureUpcomingTransactionFetcher_isAllowedTo_returnNull() {
        when(upcomingTransactionFetcher.fetchUpcomingTransactionsFor(account)).thenReturn(null);

        Collection<AggregationTransaction> transactions = fetcherController.fetchTransactionsFor(account);

        verify(upcomingTransactionFetcher).fetchUpcomingTransactionsFor(account);
        Assert.assertTrue(transactions.isEmpty());
    }

    @Test
    public void ensureUpcomingTransactions_areFetched_whenAccountType_isCreditCard() {
        CreditCardAccount creditCardAccount = CreditCardAccount.builder("uniqueIdentifier", Amount.inSEK(-2500.0),
                Amount.inSEK(7500.0))
                .setAccountNumber("0123456789")
                .setName("Credit-card")
                .build();

        new TransactionFetcherController<>(paginationHelper, creditCardAccountPaginator, upcomingTransactionFetcher)
                .fetchTransactionsFor(creditCardAccount);

        verify(upcomingTransactionFetcher, atLeastOnce()).fetchUpcomingTransactionsFor(creditCardAccount);
    }
}
