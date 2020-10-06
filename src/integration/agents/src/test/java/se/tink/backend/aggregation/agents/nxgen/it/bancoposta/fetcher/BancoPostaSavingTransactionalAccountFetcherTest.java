package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.SavingAccUrl;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.BancoPostaSavingTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BancoPostaSavingTransactionalAccountFetcherTest {
    private BancoPostaSavingTransactionalAccountFetcher objUnderTest;
    private TinkHttpClient httpClient;

    @Before
    public void init() {
        this.httpClient = mock(TinkHttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        PersistentStorage persistentStorage = FetcherTestHelper.prepareMockedPersistenStorage();
        BancoPostaStorage storage = new BancoPostaStorage(persistentStorage);
        BancoPostaApiClient apiClient = new BancoPostaApiClient(httpClient, storage);
        this.objUnderTest = new BancoPostaSavingTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccountsWithDetails() {
        // given

        RequestBuilder fetchAccountMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(
                        SavingAccUrl.FETCH_SAVING_ACCOUNTS, httpClient);
        when(fetchAccountMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getSavingAccountsResponse());

        RequestBuilder fetchAccountDetailsMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(
                        SavingAccUrl.FETCH_SAVING_ACCOUNTS_DETAILS, httpClient);
        when(fetchAccountDetailsMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getSavingAccountsDetailsResponse());
        // when

        Collection<TransactionalAccount> accounts = objUnderTest.fetchAccounts();
        // then

        assertThat(accounts).hasSize(2);
        assertThat(accounts)
                .extracting(TransactionalAccount::getType)
                .contains(AccountTypes.SAVINGS);
        assertThat(accounts.stream().allMatch(acc -> acc instanceof TransactionalAccount))
                .isEqualTo(true);
        verify(httpClient, times(2)).request(SavingAccUrl.FETCH_SAVING_ACCOUNTS_DETAILS);
    }

    @Test
    public void
            fetchAccountsShouldRespondEmptyListIfAccountsNotAvailableInResponseAndNotCallForAccountDetails() {
        // given

        RequestBuilder fetchAccountMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(
                        SavingAccUrl.FETCH_SAVING_ACCOUNTS, httpClient);
        when(fetchAccountMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getEmptySavingAccountsResponse());
        // when

        Collection<TransactionalAccount> accounts = objUnderTest.fetchAccounts();
        // then

        assertThat(accounts).isEmpty();
        verify(httpClient, never()).request(SavingAccUrl.FETCH_SAVING_ACCOUNTS_DETAILS);
    }

    @SneakyThrows
    @Test
    public void shouldFetchTransactionsAndCanFetchMoreTrueIfTransactionsListIsNotEmpty() {
        // given

        RequestBuilder fetchTransactionMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(
                        SavingAccUrl.FETCH_SAVING_TRANSACTIONS, httpClient);
        when(fetchTransactionMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getSavingTransactionsResponse());

        Account account = mock(Account.class);
        when(account.getExactBalance()).thenReturn(ExactCurrencyAmount.of("0", "EUR"));
        when(account.getApiIdentifier()).thenReturn("123456789");
        // when

        PaginatorResponse response = objUnderTest.getTransactionsFor(account, 0);
        // then

        assertThat(response.getTinkTransactions()).hasSize(2);
        assertThat(response.canFetchMore()).isEqualTo(Optional.of(false));
    }
}
