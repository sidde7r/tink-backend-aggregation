package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SocieteGeneraleTransactionalAccountFetcherTest {

    private SocieteGeneraleApiClient apiClient;
    private AccountsResponse accountsResponse;

    @Before
    public void init() {
        apiClient = mock(SocieteGeneraleApiClient.class);

        final EndUserIdentityResponse user = mock(EndUserIdentityResponse.class);
        accountsResponse = mock(AccountsResponse.class);

        when(apiClient.getEndUserIdentity()).thenReturn(user);
        when(user.getConnectedPsu()).thenReturn("connectedPsu");
    }

    @Test
    public void shouldReturnProperAccount() {
        // given
        AccountsItemEntity accountsItemEntity = mock(AccountsItemEntity.class);
        List<AccountsItemEntity> cashAccounts = new ArrayList<>();
        cashAccounts.add(accountsItemEntity);
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        Optional<TransactionalAccount> optionalTransactionalAccount =
                Optional.ofNullable(transactionalAccount);

        when(accountsItemEntity.toTinkModel(any())).thenReturn(optionalTransactionalAccount);
        when(accountsResponse.getCashAccounts()).thenReturn(cashAccounts);
        when(apiClient.fetchAccounts()).thenReturn(accountsResponse);

        SocieteGeneraleTransactionalAccountFetcher societeGeneraleTransactionalAccountFetcher =
                new SocieteGeneraleTransactionalAccountFetcher(apiClient);

        // when
        Collection<TransactionalAccount> accounts =
                societeGeneraleTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(accounts);
        assertEquals(1, accounts.size());
        assertTrue(accounts.contains(transactionalAccount));
    }

    @Test
    public void shouldReturnEmptyListOfAccountsWhenApiReturnsNullAccounts() {
        // given
        when(apiClient.fetchAccounts()).thenReturn(null);
        SocieteGeneraleTransactionalAccountFetcher societeGeneraleTransactionalAccountFetcher =
                new SocieteGeneraleTransactionalAccountFetcher(apiClient);

        // when
        Collection<TransactionalAccount> accounts =
                societeGeneraleTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }
}
