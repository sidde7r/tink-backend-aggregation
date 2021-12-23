package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.transactionalaccount;

import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@RunWith(JUnitParamsRunner.class)
public class SibsTransactionalAccountRefreshControllerTest {

  @Mock
  private MetricRefreshController metricRefreshController;

  private final TinkHttpClient client =
      NextGenTinkHttpClient.builder(
              new FakeLogMasker(),
              LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
          .build();

  private TransactionalAccountRefreshController refreshController;


  @Before
  public void setUp(){
    MockitoAnnotations.openMocks(this);

    refreshController = new TransactionalAccountRefreshController(
        metricRefreshController,
    )
  }

  @Test
  public void shouldFetchAccounts(){
    //when
    FetchAccountsResponse result = refreshController.fetchCheckingAccounts();

    //then

  }

  @Test
  public void shouldThrowBankSideErrorWhileFetchingAccounts(){

  }


}
