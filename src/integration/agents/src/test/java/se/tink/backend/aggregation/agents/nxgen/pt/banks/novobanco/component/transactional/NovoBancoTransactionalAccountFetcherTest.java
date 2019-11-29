package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.transactional.detail.AccountsTestData.getReferenceAccountDto;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.transactional.detail.AccountsTestData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.TransactionalAccountDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.NovoBancoTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NovoBancoTransactionalAccountFetcherTest {

    @Test
    public void shouldReturnNonEmptyCollectionIfTransactionalAccountsAvailable() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getAccount(AccountsTestData.PAYLOAD_ACCOUNT_ID_1))
                .thenReturn(AccountsTestData.getResponse(AccountsTestData.PAYLOAD_ACCOUNT_ID_1));
        when(apiClient.getAccount(AccountsTestData.PAYLOAD_ACCOUNT_ID_2))
                .thenReturn(AccountsTestData.getResponse(AccountsTestData.PAYLOAD_ACCOUNT_ID_2));
        when(apiClient.getAccounts())
                .thenReturn(AccountsTestData.getResponse(AccountsTestData.PAYLOAD_ALL_ACCOUNTS));
        NovoBancoTransactionalAccountFetcher fetcher =
                new NovoBancoTransactionalAccountFetcher(apiClient);

        // when & then
        assertFalse(fetcher.fetchAccounts().isEmpty());
    }

    @Test
    public void shouldReturnEmptyCollectionIfNoTransactionalAccountsAvailable() {
        // given
        final GetAccountsResponse emptyResponse = new GetAccountsResponse();
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getAccount(any())).thenReturn(emptyResponse);
        when(apiClient.getAccounts()).thenReturn(emptyResponse);
        NovoBancoTransactionalAccountFetcher fetcher =
                new NovoBancoTransactionalAccountFetcher(apiClient);

        // when & then
        assertTrue(fetcher.fetchAccounts().isEmpty());
    }

    @Test
    public void shouldReturnEmptyCollectionIfErroredResponse() {
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getAccount(any()))
                .thenReturn(AccountsTestData.getResponse(AccountsTestData.PAYLOAD_ERRORED));
        when(apiClient.getAccounts())
                .thenReturn(AccountsTestData.getResponse(AccountsTestData.PAYLOAD_ERRORED));
        NovoBancoTransactionalAccountFetcher fetcher =
                new NovoBancoTransactionalAccountFetcher(apiClient);

        // when & then
        assertTrue(fetcher.fetchAccounts().isEmpty());
    }

    @Test
    public void shouldReturnCorrectlyMappedAccounts() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getAccount(AccountsTestData.PAYLOAD_ACCOUNT_ID_1))
                .thenReturn(AccountsTestData.getResponse(AccountsTestData.PAYLOAD_ACCOUNT_ID_1));
        when(apiClient.getAccount(AccountsTestData.PAYLOAD_ACCOUNT_ID_2))
                .thenReturn(AccountsTestData.getResponse(AccountsTestData.PAYLOAD_ACCOUNT_ID_2));
        when(apiClient.getAccounts())
                .thenReturn(AccountsTestData.getResponse(AccountsTestData.PAYLOAD_ALL_ACCOUNTS));
        NovoBancoTransactionalAccountFetcher fetcher =
                new NovoBancoTransactionalAccountFetcher(apiClient);

        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertEquals(2, accounts.size());
        assertAccountsEqual(accounts);
    }

    private void assertAccountsEqual(Collection<TransactionalAccount> accounts) {
        accounts.forEach(
                account -> {
                    TransactionalAccountDto expected =
                            getReferenceAccountDto(account.getAccountNumber());
                    assertAccountEquals(expected, account);
                });
    }

    private void assertAccountEquals(
            TransactionalAccountDto expected, TransactionalAccount account) {
        assertEquals(expected.getAccountNumber(), account.getAccountNumber());
        assertTrue(account.isUniqueIdentifierEqual(expected.getUniqueIdentifier()));
        assertEquals(expected.getExactBalance(), account.getExactBalance());
    }
}
