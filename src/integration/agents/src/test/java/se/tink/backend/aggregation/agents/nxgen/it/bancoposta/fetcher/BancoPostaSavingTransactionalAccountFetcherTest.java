package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.SavingAccounts;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.BancoPostaSavingTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BancoPostaSavingTransactionalAccountFetcherTest {
    private BancoPostaSavingTransactionalAccountFetcher objUnderTest;
    private TinkHttpClient httpClient;
    private static final String OTP_SECRET_KEY =
            "GIYXQ53CGI2TC6DMM5TWK5RVNBTGC6DOORVG253CNRXWI23IOQ3G63TSN5VHEOLMPFWGYNRXNEZDIYTGMJXWUMJTHFUHO6RY";
    private static final String APP_ID = "appId";
    private static final String ACCESS_TOKEN = "accessToken";

    @Before
    public void init() {
        this.httpClient = mock(TinkHttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        BancoPostaStorage storage = new BancoPostaStorage(persistentStorage);
        BancoPostaApiClient apiClient = new BancoPostaApiClient(httpClient, storage);
        this.objUnderTest = new BancoPostaSavingTransactionalAccountFetcher(apiClient);

        when(persistentStorage.get(Storage.ACCESS_BASIC_TOKEN)).thenReturn(ACCESS_TOKEN);
        when(persistentStorage.get(Storage.OTP_SECRET_KEY)).thenReturn(OTP_SECRET_KEY);
        when(persistentStorage.get(Storage.APP_ID)).thenReturn(APP_ID);
    }

    private RequestBuilder mockRequestBuilder(URL url) {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(httpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), any())).thenReturn(requestBuilder);
        return requestBuilder;
    }

    @Test
    public void shouldFetchAccountsWithDetails() {
        // given

        RequestBuilder fetchAccountMockRequestBuilder =
                mockRequestBuilder(SavingAccounts.FETCH_SAVING_ACCOUNTS);
        when(fetchAccountMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getSavingAccountsResponse());

        RequestBuilder fetchAccountDetailsMockRequestBuilder =
                mockRequestBuilder(SavingAccounts.FETCH_SAVING_ACCOUNTS_DETAILS);
        when(fetchAccountDetailsMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getSavingAccountsDetailsResponse());
        // when

        Collection<TransactionalAccount> accounts = objUnderTest.fetchAccounts();
        // then

        assertThat(accounts).hasSize(1);
        verify(httpClient, times(1)).request(SavingAccounts.FETCH_SAVING_ACCOUNTS_DETAILS);
    }

    @Test
    public void shouldFetchTransactionsAndCanFetchMoreTrueIfTransactionsListIsNotEmpty() {
        // given

        RequestBuilder fetchTransactionMockRequestBuilder =
                mockRequestBuilder(SavingAccounts.FETCH_SAVING_TRANSACTIONS);
        when(fetchTransactionMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getSavingTransactionsResponse());

        Account account = mock(Account.class);
        given(account.getExactBalance()).willReturn(ExactCurrencyAmount.of("0", "EUR"));
        when(account.getApiIdentifier()).thenReturn("123456789");

        // when

        PaginatorResponse response = objUnderTest.getTransactionsFor(account, 0);
        // then

        assertThat(response.getTinkTransactions()).hasSize(1);
        assertThat(response.canFetchMore()).isEqualTo(Optional.of(false));
    }
}
