package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.Account;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ProductionUrls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.EmptyFinalPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class IcaBankenTransactionFetcherTest {
    private TinkHttpClient client;
    private IcaBankenTransactionFetcher icaBankenTransactionFetcher;
    private PersistentStorage persistentStorage;
    private static final int MAX_NUM_MONTHS_TO_FETCH = 18;

    @Before
    public void setUp() {
        client = mock(TinkHttpClient.class);
        persistentStorage = mock(PersistentStorage.class);
        IcaBankenApiClient apiClient = new IcaBankenApiClient(client, persistentStorage);
        LocalDateTimeSource localDateTimeSource = mock(LocalDateTimeSource.class);
        when(localDateTimeSource.now()).thenReturn(LocalDateTime.now());
        icaBankenTransactionFetcher =
                new IcaBankenTransactionFetcher(apiClient, localDateTimeSource);
    }

    @Test
    public void shouldThrowHttpResponseException() {
        // given
        Date fromDate =
                Date.from(
                        LocalDate.now()
                                .minusMonths(MAX_NUM_MONTHS_TO_FETCH - 10)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant());
        Date toDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpResponseException exception = new HttpResponseException(httpRequest, httpResponse);
        TransactionalAccount account = mock(TransactionalAccount.class);

        // when
        Mockito.when(account.getApiIdentifier()).thenThrow(exception);
        Mockito.when(httpResponse.getBody(String.class)).thenReturn("Bad Request");

        // then
        assertThatThrownBy(
                        () ->
                                icaBankenTransactionFetcher.getTransactionsFor(
                                        account, fromDate, toDate))
                .isInstanceOf(HttpResponseException.class);
    }

    @Test
    public void shouldReturnEmptyPaginationResponseWhenTransactionNotFoundException() {
        // given
        Date fromDate =
                Date.from(
                        LocalDate.now()
                                .minusMonths(MAX_NUM_MONTHS_TO_FETCH - 10)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant());
        Date toDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpResponseException exception = new HttpResponseException(httpRequest, httpResponse);
        TransactionalAccount account = mock(TransactionalAccount.class);

        // when
        Mockito.when(account.getApiIdentifier()).thenThrow(exception);
        Mockito.when(httpResponse.getBody(String.class))
                .thenReturn(IcaBankenConstants.TransactionResponse.TRANSACTION_NOT_FOUND);
        PaginatorResponse paginatorResponse =
                icaBankenTransactionFetcher.getTransactionsFor(account, fromDate, toDate);

        // then
        Assert.assertNotNull(paginatorResponse);
    }

    @Test
    public void shouldReturnEmptyFinalPaginationResponseWhenFromDateBeforeLimitDate() {
        // given
        Date fromDate =
                Date.from(
                        LocalDate.now()
                                .minusMonths(MAX_NUM_MONTHS_TO_FETCH + 1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant());
        Date toDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        TransactionalAccount account = mock(TransactionalAccount.class);
        PaginatorResponse paginatorResponse =
                icaBankenTransactionFetcher.getTransactionsFor(account, fromDate, toDate);

        // then
        Assert.assertNotNull(paginatorResponse);
        Assert.assertEquals(paginatorResponse.getClass(), EmptyFinalPaginatorResponse.class);
    }

    @Test
    public void shouldReturnPaginatedTransactionsWhenToDateBeforeLimitDate() {
        // given
        Date fromDate =
                Date.from(
                        LocalDate.now()
                                .minusMonths(MAX_NUM_MONTHS_TO_FETCH - 10)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant());
        Date toDate =
                Date.from(
                        LocalDate.now()
                                .minusMonths(MAX_NUM_MONTHS_TO_FETCH + 1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant());
        TransactionalAccount account = mock(TransactionalAccount.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        URL baseUrl = new URL(ProductionUrls.TRANSACTIONS_PATH);
        URL requestUrl = baseUrl.parameter(Account.ACCOUNT_ID, "apiIdentifier");
        FetchTransactionsResponse transactionsResponse = mock(FetchTransactionsResponse.class);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);

        // when
        Mockito.when(account.getApiIdentifier()).thenReturn("apiIdentifier");
        Mockito.when(client.request(requestUrl)).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.queryParam(Mockito.any(), Mockito.any()))
                .thenReturn(requestBuilder, requestBuilder, requestBuilder);
        Mockito.when(requestBuilder.header(Mockito.any(), Mockito.any()))
                .thenReturn(requestBuilder, requestBuilder);
        Mockito.when(requestBuilder.addBearerToken(Mockito.any())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.get(FetchTransactionsResponse.class))
                .thenReturn(transactionsResponse);
        Mockito.when(persistentStorage.get(StorageKeys.TOKEN, OAuth2Token.class))
                .thenReturn(Optional.ofNullable(oAuth2Token));
        PaginatorResponse paginatorResponse =
                icaBankenTransactionFetcher.getTransactionsFor(account, fromDate, toDate);

        // then
        Assert.assertNotNull(paginatorResponse);
        Assert.assertEquals(paginatorResponse.hashCode(), transactionsResponse.hashCode());
    }
}
