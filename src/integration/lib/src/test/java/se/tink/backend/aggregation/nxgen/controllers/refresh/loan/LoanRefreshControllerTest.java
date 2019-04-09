package se.tink.backend.aggregation.nxgen.controllers.refresh.loan;

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
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.metrics.MetricId;

@RunWith(MockitoJUnitRunner.class)
public class LoanRefreshControllerTest {
    @Mock private MetricRefreshController metricRefreshController;
    @Mock private UpdateController updateController;
    @Mock private AccountFetcher<LoanAccount> loanFetcher;
    private LoanRefreshController loanRefresher;

    @Before
    public void setup() {
        Mockito.when(
                        metricRefreshController.buildAction(
                                Mockito.any(MetricId.class), Mockito.anyList()))
                .thenReturn(Mockito.mock(MetricRefreshAction.class));
        loanRefresher =
                new LoanRefreshController(metricRefreshController, updateController, loanFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenMetricRefreshController_isNull() {
        new LoanRefreshController(null, updateController, loanFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenContext_isNull() {
        new LoanRefreshController(metricRefreshController, null, loanFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenLoanFetcher_isNull() {
        new LoanRefreshController(metricRefreshController, updateController, null);
    }

    @Test
    public void ensureNullLoanAccountsMap_isConverted_toEmptyMap() {
        Mockito.when(loanFetcher.fetchAccounts()).thenReturn(null);

        loanRefresher.fetchAccounts();

        Mockito.verify(loanFetcher).fetchAccounts();
        Mockito.verifyZeroInteractions(updateController);
    }
}
