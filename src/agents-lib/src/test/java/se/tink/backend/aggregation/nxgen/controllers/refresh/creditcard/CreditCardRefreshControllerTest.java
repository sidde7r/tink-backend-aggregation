package se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
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
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TestAccountBuilder;
import se.tink.libraries.metrics.MetricId;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CreditCardRefreshControllerTest {
    @Mock
    private MetricRefreshController metricRefreshController;
    @Mock
    private UpdateController updateController;
    @Mock
    private AccountFetcher<CreditCardAccount> accountFetcher;
    @Mock
    private TransactionFetcher<CreditCardAccount> transactionFetcher;
    private CreditCardRefreshController creditCardRefresher;

    @Before
    public void setup() {
        Mockito.when(metricRefreshController.buildAction(Mockito.any(MetricId.class), Mockito.anyList()))
                .thenReturn(Mockito.mock(MetricRefreshAction.class));
        creditCardRefresher = new CreditCardRefreshController(metricRefreshController, updateController, accountFetcher,
                transactionFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenMetricRefreshController_isNull() {
        new CreditCardRefreshController(null, updateController, accountFetcher, transactionFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenContext_isNull() {
        new CreditCardRefreshController(metricRefreshController, null, accountFetcher, transactionFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenAccountFetcher_isNull() {
        new CreditCardRefreshController(metricRefreshController, updateController, null, transactionFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenTransactionFetcher_isNull() {
        new CreditCardRefreshController(metricRefreshController, updateController, accountFetcher, null);
    }

    @Test
    public void ensureNullAccountsList_isConverted_toEmptyList() {
        Mockito.when(accountFetcher.fetchAccounts()).thenReturn(null);

        creditCardRefresher.fetchAccounts();

        Mockito.verify(accountFetcher).fetchAccounts();
        Mockito.verify(updateController, Mockito.never()).updateAccount(Mockito.any(CreditCardAccount.class));
    }

    @Test
    public void ensureNullTransactionsList_isConverted_toEmptyList() {
        List<CreditCardAccount> accounts = ImmutableList.of(TestAccountBuilder.from(CreditCardAccount.class).build());
        Mockito.when(accountFetcher.fetchAccounts()).thenReturn(accounts);
        Mockito.when(transactionFetcher.fetchTransactionsFor(Mockito.any(CreditCardAccount.class))).thenReturn(null);

        creditCardRefresher.fetchTransactions();

        Mockito.verify(accountFetcher).fetchAccounts();
        Mockito.verify(transactionFetcher).fetchTransactionsFor(Mockito.any(CreditCardAccount.class));
        Mockito.verify(updateController).updateTransactions(accounts.get(0), Collections.emptyList());
    }
}
