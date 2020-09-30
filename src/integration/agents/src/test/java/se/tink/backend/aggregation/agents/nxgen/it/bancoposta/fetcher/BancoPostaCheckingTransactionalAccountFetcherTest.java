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
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.BancoPostaCheckingTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BancoPostaCheckingTransactionalAccountFetcherTest {
    private BancoPostaCheckingTransactionalAccountFetcher objUnderTest;
    private BancoPostaApiClient apiClient;

    @Before
    public void init() {
        this.apiClient = mock(BancoPostaApiClient.class);
        this.objUnderTest = new BancoPostaCheckingTransactionalAccountFetcher(this.apiClient);
    }

    @Test
    public void shouldFetchAccountsWithDetails() {
        // given
        given(apiClient.fetchAccounts()).willReturn(FetcherTestData.getAccountResponse());
        given(apiClient.fetchAccountDetails(any()))
                .willReturn(FetcherTestData.getAccountDetailsResponse());
        // when
        Collection<TransactionalAccount> accounts = objUnderTest.fetchAccounts();
        // then
        assertThat(accounts).hasSize(2);
        verify(apiClient, times(2)).fetchAccountDetails(any());
    }

    @Test
    public void shouldFetchTransactionsAndCanFetchMoreTrueIfTransactionsListIsNotEmpty() {
        // given
        given(apiClient.fetchTransactions(any()))
                .willReturn(FetcherTestData.getCheckingTransactionResponse());
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
        given(apiClient.fetchTransactions(any()))
                .willReturn(FetcherTestData.getEmptyCheckingTransactionResponse());
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
