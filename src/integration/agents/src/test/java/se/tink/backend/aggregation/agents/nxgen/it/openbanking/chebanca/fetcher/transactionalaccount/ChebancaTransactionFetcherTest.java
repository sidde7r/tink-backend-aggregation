package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount;

import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.data.TransactionTestData;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class ChebancaTransactionFetcherTest {

    private static final int ERROR_RESPONSE_CODE = 404;
    private static final int SUCCESSFUL_RESPONSE_CODE = 200;
    private static final String MOCKED_API_ID = "mocked_id";
    private static final Date SOME_DATE = new Date();
    private ChebancaApiClient apiClient;
    private final TransactionalAccount account = mock(TransactionalAccount.class);

    @Test
    public void shouldReturnProperAmountOfTransactions() {
        // given
        init(getMockedSuccessfulTransactionsResponse());
        ChebancaTransactionFetcher fetcher = new ChebancaTransactionFetcher(apiClient);

        // when
        PaginatorResponse transactions = fetcher.getTransactionsFor(account, SOME_DATE, SOME_DATE);

        // then
        assertEquals(4, transactions.getTinkTransactions().size());
    }

    @Test
    public void shouldThrowIfFetchingFailed() {
        // given
        init(getMockedFailedTransactionsResponse());
        ChebancaTransactionFetcher fetcher = new ChebancaTransactionFetcher(apiClient);

        // when
        Throwable thrown =
                catchThrowable(() -> fetcher.getTransactionsFor(account, SOME_DATE, SOME_DATE));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not fetch transactions. Error response code: "
                                + ERROR_RESPONSE_CODE);
    }

    private void init(HttpResponse response) {
        apiClient = mock(ChebancaApiClient.class);
        when(apiClient.getTransactions(any(), any(), any())).thenReturn(response);
        when(account.getApiIdentifier()).thenReturn(MOCKED_API_ID);
    }

    private HttpResponse getMockedSuccessfulTransactionsResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(GetTransactionsResponse.class))
                .thenReturn(TransactionTestData.getTransactionsResponse());
        return response;
    }

    private HttpResponse getMockedFailedTransactionsResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(ERROR_RESPONSE_CODE);
        return response;
    }
}
