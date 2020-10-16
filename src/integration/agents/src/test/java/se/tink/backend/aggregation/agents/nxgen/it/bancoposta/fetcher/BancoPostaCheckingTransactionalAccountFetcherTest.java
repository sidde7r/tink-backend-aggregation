package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.CheckingAccUrl;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.BancoPostaCheckingTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BancoPostaCheckingTransactionalAccountFetcherTest {
    private BancoPostaCheckingTransactionalAccountFetcher objUnderTest;
    private TinkHttpClient httpClient;

    private static final String ACCOUNTS_RESPONSE_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/bancoposta/resources/accountResponse.json";

    private static final String ACCOUNTS_DETAILS_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/bancoposta/resources/accountDetails.json";

    private static final String TRANSACTIONS_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/bancoposta/resources/accountTransactionResponse.json";

    private static final String EMPTY_TRANSACTIONS_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/bancoposta/resources/emptyTransactionResponse.json";

    private static final AccountsResponse ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(ACCOUNTS_RESPONSE_FILE_PATH), AccountsResponse.class);

    private static final AccountDetailsResponse ACCOUNTS_DETAILS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(ACCOUNTS_DETAILS_FILE_PATH), AccountDetailsResponse.class);

    private static final TransactionsResponse TRANSACTIONS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(TRANSACTIONS_FILE_PATH), TransactionsResponse.class);

    private static final TransactionsResponse EMPTY_TRANSACTIONS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(EMPTY_TRANSACTIONS_FILE_PATH), TransactionsResponse.class);

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
                FetcherTestHelper.mockRequestBuilder(CheckingAccUrl.FETCH_ACCOUNTS, httpClient);
        when(fetchAccountMockRequestBuilder.post(any(), any())).thenReturn(ACCOUNTS_RESPONSE);

        RequestBuilder fetchAccountDetailsMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(
                        CheckingAccUrl.FETCH_ACCOUNT_DETAILS, httpClient);
        when(fetchAccountDetailsMockRequestBuilder.post(any(), any()))
                .thenReturn(ACCOUNTS_DETAILS_RESPONSE);
        // when

        Collection<TransactionalAccount> accounts = objUnderTest.fetchAccounts();
        // then

        assertThat(accounts).hasSize(2);
        verify(httpClient, times(2)).request(CheckingAccUrl.FETCH_ACCOUNT_DETAILS);
    }

    @Test
    public void shouldFetchTransactionsAndCanFetchMoreTrueIfTransactionsListIsNotEmpty() {
        // given

        RequestBuilder fetchTransactionMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(CheckingAccUrl.FETCH_TRANSACTIONS, httpClient);
        when(fetchTransactionMockRequestBuilder.post(any(), any()))
                .thenReturn(TRANSACTIONS_RESPONSE);

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
                FetcherTestHelper.mockRequestBuilder(CheckingAccUrl.FETCH_TRANSACTIONS, httpClient);
        when(fetchTransactionMockRequestBuilder.post(any(), any()))
                .thenReturn(EMPTY_TRANSACTIONS_RESPONSE);
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
