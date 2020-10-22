package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
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
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.FetcherTestHelper;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.rpc.SavingAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.rpc.SavingAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.rpc.SavingTransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BancoPostaSavingTransactionalAccountFetcherTest {
    private BancoPostaSavingTransactionalAccountFetcher objUnderTest;
    private TinkHttpClient httpClient;

    private static final String SAVING_ACCOUNTS_RESPONSE_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/banks/bancoposta/resources/savingAccountResponse.json";

    private static final String EMPTY_SAVING_ACCOUNTS_RESPONSE_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/banks/bancoposta/resources/emptySavingAccountResponse.json";

    private static final String SAVING_ACCOUNTS_DETAILS_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/banks/bancoposta/resources/savingAccountDetails.json";

    private static final String SAVING_TRANSACTIONS_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/banks/bancoposta/resources/savingAccountTransactionResponse.json";

    private static final SavingAccountResponse SAVING_ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(SAVING_ACCOUNTS_RESPONSE_FILE_PATH), SavingAccountResponse.class);

    private static final SavingAccountResponse EMPTY_SAVING_ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(EMPTY_SAVING_ACCOUNTS_RESPONSE_FILE_PATH),
                    SavingAccountResponse.class);

    private static final SavingAccountDetailsResponse SAVING_ACCOUNTS_DETAILS =
            SerializationUtils.deserializeFromString(
                    new File(SAVING_ACCOUNTS_DETAILS_FILE_PATH),
                    SavingAccountDetailsResponse.class);

    private static final SavingTransactionResponse SAVING_TRANSACTIONS =
            SerializationUtils.deserializeFromString(
                    new File(SAVING_TRANSACTIONS_FILE_PATH), SavingTransactionResponse.class);

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
                .thenReturn(SAVING_ACCOUNTS_RESPONSE);

        RequestBuilder fetchAccountDetailsMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(
                        SavingAccUrl.FETCH_SAVING_ACCOUNTS_DETAILS, httpClient);
        when(fetchAccountDetailsMockRequestBuilder.post(any(), any()))
                .thenReturn(SAVING_ACCOUNTS_DETAILS);
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
                .thenReturn(EMPTY_SAVING_ACCOUNTS_RESPONSE);
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
        when(fetchTransactionMockRequestBuilder.post(any(), any())).thenReturn(SAVING_TRANSACTIONS);

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
