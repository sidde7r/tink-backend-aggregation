package se.tink.backend.aggregation.nxgen.controllers.refresh.investment;

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
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.libraries.metrics.MetricId;

@RunWith(MockitoJUnitRunner.class)
public class InvestmentRefreshControllerTest {
    @Mock
    private MetricRefreshController metricRefreshController;
    @Mock
    private UpdateController updateController;
    @Mock
    private AccountFetcher<InvestmentAccount> investmentFetcher;
    private InvestmentRefreshController investmentRefresher;

    @Before
    public void setup() {
        Mockito.when(metricRefreshController.buildAction(Mockito.any(MetricId.class), Mockito.anyList()))
                .thenReturn(Mockito.mock(MetricRefreshAction.class));
        investmentRefresher = new InvestmentRefreshController(metricRefreshController, updateController,
                investmentFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenMetricRefreshController_isNull() {
        new InvestmentRefreshController(null, updateController, investmentFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenContext_isNull() {
        new InvestmentRefreshController(metricRefreshController, null, investmentFetcher);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenInvestmentFetcher_isNull() {
        new InvestmentRefreshController(metricRefreshController, updateController, null);
    }

    @Test
    public void ensureNullInvestmentAccountsMap_isConverted_toEmptyMap() {
        Mockito.when(investmentFetcher.fetchAccounts()).thenReturn(null);

        investmentRefresher.fetchAccounts();

        Mockito.verify(investmentFetcher).fetchAccounts();
        Mockito.verifyZeroInteractions(updateController);
    }
}
