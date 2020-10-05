package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.CheckingAccounts;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.BancoPostaCheckingTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BancoPostaCheckingTransactionalAccountFetcherTest {
    private BancoPostaCheckingTransactionalAccountFetcher objUnderTest;
    private TinkHttpClient httpClient;

    @Before
    public void init() {
        this.httpClient = mock(TinkHttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        PersistentStorage persistentStorage = FetcherTestHelper.prepareMockedPersistenStorage();
        BancoPostaStorage storage = new BancoPostaStorage(persistentStorage);
        BancoPostaApiClient apiClient = new BancoPostaApiClient(httpClient, storage);
        this.objUnderTest = new BancoPostaCheckingTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccountsWithDetails() {
        // given

        RequestBuilder fetchAccountMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(CheckingAccounts.FETCH_ACCOUNTS, httpClient);
        when(fetchAccountMockRequestBuilder.get(any()))
                .thenReturn(FetcherTestData.getAccountResponse());

        RequestBuilder fetchAccountDetailsMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(
                        CheckingAccounts.FETCH_ACCOUNT_DETAILS, httpClient);
        when(fetchAccountDetailsMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getAccountDetailsResponse());
        // when

        Collection<TransactionalAccount> accounts = objUnderTest.fetchAccounts();
        // then

        assertThat(accounts).hasSize(2);
        verify(httpClient, times(2)).request(CheckingAccounts.FETCH_ACCOUNT_DETAILS);
    }

    @Test
    public void shouldFetchTransactionsAndCanFetchMoreTrueIfTransactionsListIsNotEmpty() {
        // given

        RequestBuilder fetchTransactionMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(
                        CheckingAccounts.FETCH_TRANSACTIONS, httpClient);
        when(fetchTransactionMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getCheckingTransactionResponse());

        Account account = mock(Account.class);
        given(account.getExactBalance()).willReturn(ExactCurrencyAmount.of("0", "EUR"));
        when(account.getApiIdentifier()).thenReturn("123456789");

        // when

        PaginatorResponse response = objUnderTest.getTransactionsFor(account, 0);
        // then

        assertThat(response.getTinkTransactions()).hasSize(3);
        assertThat(response.canFetchMore()).isEqualTo(Optional.of(true));
    }

    @Test
    public void shouldFetchTransactionsAndCanFetchMoreFalseIfTransactionsListIsEmpty() {
        // given

        RequestBuilder fetchTransactionMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(
                        CheckingAccounts.FETCH_TRANSACTIONS, httpClient);
        when(fetchTransactionMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getEmptyCheckingTransactionResponse());
        Account account = mock(Account.class);
        given(account.getExactBalance()).willReturn(ExactCurrencyAmount.of("0", "EUR"));
        when(account.getApiIdentifier()).thenReturn("123456789");

        // when

        PaginatorResponse response = objUnderTest.getTransactionsFor(account, 0);
        // then

        assertThat(response.getTinkTransactions()).hasSize(0);
        assertThat(response.canFetchMore()).isEqualTo(Optional.of(false));
    }
}
