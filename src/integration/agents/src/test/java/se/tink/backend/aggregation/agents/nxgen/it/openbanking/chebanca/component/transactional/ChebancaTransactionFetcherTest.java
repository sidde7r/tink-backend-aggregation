package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.component.transactional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.component.transactional.data.TransactionTestData;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.ChebancaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class ChebancaTransactionFetcherTest {
    private ChebancaApiClient apiClient;
    private final String MOCKED_API_ID = "mocked_id";
    private final Date SOME_DATE = new Date();
    private final TransactionalAccount account = mock(TransactionalAccount.class);

    @Test
    public void shouldReturnProperAmountOfTransactions() {
        // given
        init(getMockedSuccessfulTransactionsResponse());
        ChebancaTransactionFetcher fetcher = new ChebancaTransactionFetcher(apiClient);

        // when
        PaginatorResponse transactions = fetcher.getTransactionsFor(account, SOME_DATE, SOME_DATE);

        // then
        assertEquals(10, transactions.getTinkTransactions().size());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfFetchingFailed() {
        // given
        init(getMockedFailedTransactionsResponse());
        ChebancaTransactionFetcher fetcher = new ChebancaTransactionFetcher(apiClient);

        // when
        PaginatorResponse transactions = fetcher.getTransactionsFor(account, SOME_DATE, SOME_DATE);
    }

    private void init(HttpResponse response) {
        apiClient = mock(ChebancaApiClient.class);
        when(apiClient.getTransactions(any(), any(), any())).thenReturn(response);
        when(account.getApiIdentifier()).thenReturn(MOCKED_API_ID);
    }

    private HttpResponse getMockedSuccessfulTransactionsResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody(GetTransactionsResponse.class))
                .thenReturn(TransactionTestData.getTransactionsResponse());
        return response;
    }

    private HttpResponse getMockedFailedTransactionsResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(404);
        return response;
    }
}
