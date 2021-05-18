package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount;

import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.data.TransactionTestData.PagedTransactionTestData;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ChebancaTransactionFetcherTest {

    private static final int ERROR_RESPONSE_CODE = 404;
    private static final int SUCCESSFUL_RESPONSE_CODE = 200;
    private static final String MOCKED_API_ID = "mocked_id";
    private static final Date SOME_DATE = new Date();
    private static final Date PAGING_FROM = new Date(150, Calendar.JUNE, 30);
    private static final Date PAGING_TO = new Date(150, Calendar.DECEMBER, 31);
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/chebanca/resources";
    private ChebancaApiClient apiClient;
    private final TransactionalAccount account = mock(TransactionalAccount.class);

    @Test
    public void shouldReturnProperAmountOfTransactions() {
        // given
        GetTransactionsResponse transactionsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactions_response.json").toFile(),
                        GetTransactionsResponse.class);

        init(getMockedSuccessfulTransactionsResponse(transactionsResponse));
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

    @Test
    public void shouldMakeNeededCallsToFetchTransactionsPagedOnlyByNextAccountingIndex() {
        // given
        apiClient = mock(ChebancaApiClient.class);
        HttpResponse firstFetch =
                getMockedSuccessfulTransactionsResponse(
                        PagedTransactionTestData.getFirstResponseForScenario1());
        HttpResponse secondFetch =
                getMockedSuccessfulTransactionsResponse(
                        PagedTransactionTestData.getSecondResponseForScenario1());

        when(apiClient.getTransactions(
                        any(), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull()))
                .thenReturn(firstFetch);
        when(apiClient.getTransactions(
                        any(), same(PAGING_FROM), same(PAGING_TO), eq(11L), isNull()))
                .thenReturn(secondFetch);
        when(account.getApiIdentifier()).thenReturn(MOCKED_API_ID);

        ChebancaTransactionFetcher fetcher = new ChebancaTransactionFetcher(apiClient);

        // when
        fetcher.getTransactionsFor(account, PAGING_FROM, PAGING_TO);

        // then
        verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull());
        verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), eq(11L), isNull());
        verifyNoMoreInteractions(apiClient);

        // and also then
        InOrder orderVerifier = inOrder(apiClient);
        orderVerifier
                .verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull());
        orderVerifier
                .verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), eq(11L), isNull());
    }

    @Test
    public void shouldMakeNeededCallsToFetchTransactionsPagedOnlyByNextNotAccountingIndex() {
        // given
        apiClient = mock(ChebancaApiClient.class);
        HttpResponse firstFetch =
                getMockedSuccessfulTransactionsResponse(
                        PagedTransactionTestData.getFirstResponseForScenario2());
        HttpResponse secondFetch =
                getMockedSuccessfulTransactionsResponse(
                        PagedTransactionTestData.getSecondResponseForScenario2());

        when(apiClient.getTransactions(
                        any(), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull()))
                .thenReturn(firstFetch);
        when(apiClient.getTransactions(any(), same(PAGING_FROM), same(PAGING_TO), isNull(), eq(2L)))
                .thenReturn(secondFetch);
        when(account.getApiIdentifier()).thenReturn(MOCKED_API_ID);

        ChebancaTransactionFetcher fetcher = new ChebancaTransactionFetcher(apiClient);

        // when
        fetcher.getTransactionsFor(account, PAGING_FROM, PAGING_TO);

        // then
        verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull());
        verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), isNull(), eq(2L));
        verifyNoMoreInteractions(apiClient);

        // and also then
        InOrder orderVerifier = inOrder(apiClient);
        orderVerifier
                .verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull());
        orderVerifier
                .verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), isNull(), eq(2L));
    }

    @Test
    public void
            shouldMakeNeededCallsToFetchTransactionsPagedByBothNextAccountingAndNextNotAccountingIndexes() {
        // given
        apiClient = mock(ChebancaApiClient.class);
        HttpResponse firstFetch =
                getMockedSuccessfulTransactionsResponse(
                        PagedTransactionTestData.getFirstResponseForScenario3());
        HttpResponse secondFetch =
                getMockedSuccessfulTransactionsResponse(
                        PagedTransactionTestData.getSecondResponseForScenario3());
        HttpResponse thirdFetch =
                getMockedSuccessfulTransactionsResponse(
                        PagedTransactionTestData.getThirdResponseForScenario3());

        when(apiClient.getTransactions(
                        any(), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull()))
                .thenReturn(firstFetch);
        when(apiClient.getTransactions(any(), same(PAGING_FROM), same(PAGING_TO), eq(12L), eq(2L)))
                .thenReturn(secondFetch);
        when(apiClient.getTransactions(
                        any(), same(PAGING_FROM), same(PAGING_TO), eq(14L), isNull()))
                .thenReturn(thirdFetch);
        when(account.getApiIdentifier()).thenReturn(MOCKED_API_ID);

        ChebancaTransactionFetcher fetcher = new ChebancaTransactionFetcher(apiClient);

        // when
        fetcher.getTransactionsFor(account, PAGING_FROM, PAGING_TO);

        // then
        verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull());
        verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), eq(12L), eq(2L));
        verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), eq(14L), isNull());
        verifyNoMoreInteractions(apiClient);

        // and also then
        InOrder orderVerifier = inOrder(apiClient);
        orderVerifier
                .verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull());
        orderVerifier
                .verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), eq(12L), eq(2L));
        orderVerifier
                .verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), eq(14L), isNull());
    }

    @Test
    public void shouldGetAllTransactionsAtOnceIfNoPagedTransactionsAvailable() {
        // given
        apiClient = mock(ChebancaApiClient.class);
        HttpResponse firstFetch =
                getMockedSuccessfulTransactionsResponse(
                        PagedTransactionTestData.getResponseForScenario4());

        when(apiClient.getTransactions(
                        any(), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull()))
                .thenReturn(firstFetch);
        when(account.getApiIdentifier()).thenReturn(MOCKED_API_ID);

        ChebancaTransactionFetcher fetcher = new ChebancaTransactionFetcher(apiClient);

        // when
        fetcher.getTransactionsFor(account, PAGING_FROM, PAGING_TO);

        // then
        verify(apiClient)
                .getTransactions(
                        eq(MOCKED_API_ID), same(PAGING_FROM), same(PAGING_TO), isNull(), isNull());
        verifyNoMoreInteractions(apiClient);
    }

    private void init(HttpResponse response) {
        apiClient = mock(ChebancaApiClient.class);
        when(apiClient.getTransactions(any(), any(), any(), any(), any())).thenReturn(response);
        when(account.getApiIdentifier()).thenReturn(MOCKED_API_ID);
    }

    private HttpResponse getMockedSuccessfulTransactionsResponse(GetTransactionsResponse body) {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(GetTransactionsResponse.class)).thenReturn(body);
        return response;
    }

    private HttpResponse getMockedFailedTransactionsResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(ERROR_RESPONSE_CODE);
        return response;
    }
}
