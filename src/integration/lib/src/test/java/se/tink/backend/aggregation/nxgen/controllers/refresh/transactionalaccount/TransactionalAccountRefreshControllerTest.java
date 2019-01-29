package se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.metrics.MetricId;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalAccountRefreshControllerTest {
    @Mock
    private MetricRefreshController metricRefreshController;
    @Mock
    private UpdateController updateController;
    @Mock
    private AccountFetcher<TransactionalAccount> accountFetcher;
    @Mock
    private TransactionFetcher<TransactionalAccount> transactionFetcher;
    private TransactionalAccountRefreshController transactionalAccountRefresher;

    @Before
    public void setup() {
        Mockito.when(metricRefreshController.buildAction(Mockito.any(MetricId.class), Mockito.anyList()))
                .thenReturn(Mockito.mock(MetricRefreshAction.class));
        transactionalAccountRefresher = new TransactionalAccountRefreshController(
                metricRefreshController, updateController,
                accountFetcher, transactionFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenMetricRefreshController_isNull() {
        new TransactionalAccountRefreshController(null, updateController, accountFetcher, transactionFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenContext_isNull() {
        new TransactionalAccountRefreshController(metricRefreshController, null, accountFetcher, transactionFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenAccountFetcher_isNull() {
        new TransactionalAccountRefreshController(metricRefreshController, updateController, null, transactionFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenTransactionFetcher_isNull() {
        new TransactionalAccountRefreshController(metricRefreshController, updateController, accountFetcher, null);
    }

    @Test
    public void ensureNullAccountsList_isConverted_toEmptyList() {
        Mockito.when(accountFetcher.fetchAccounts()).thenReturn(null);

        transactionalAccountRefresher.fetchAccounts();

        Mockito.verify(accountFetcher).fetchAccounts();
        Mockito.verify(updateController, Mockito.never()).updateAccount(Mockito.any(TransactionalAccount.class));
    }

    @Test
    public void ensureNullTransactionsList_isConverted_toEmptyList() {
        List<TransactionalAccount> accounts = ImmutableList.of(Mockito.mock(CheckingAccount.class));
        Mockito.when(accountFetcher.fetchAccounts()).thenReturn(accounts);
        Mockito.when(transactionFetcher.fetchTransactionsFor(Mockito.any(TransactionalAccount.class))).thenReturn(null);

        transactionalAccountRefresher.fetchTransactions();

        Mockito.verify(accountFetcher).fetchAccounts();
        Mockito.verify(transactionFetcher).fetchTransactionsFor(Mockito.any(TransactionalAccount.class));
        Mockito.verify(updateController).updateTransactions(accounts.get(0), Collections.emptyList());
    }
}
