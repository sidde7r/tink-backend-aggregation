package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AccountResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CmcicTransactionalAccountFetcherTest {

    private CmcicApiClient apiClient;
    private CmcicTransactionalAccountFetcher cmcicTransactionalAccountFetcher;
    private FetchAccountsResponse fetchAccountsResponse;
    private AccountResourceEntity accountResourceEntity;
    private TransactionalAccount transactionalAccount;

    @Before
    public void init() {
        apiClient = mock(CmcicApiClient.class);
        fetchAccountsResponse = mock(FetchAccountsResponse.class);
        accountResourceEntity = mock(AccountResourceEntity.class);
        transactionalAccount = mock(TransactionalAccount.class);

        cmcicTransactionalAccountFetcher = new CmcicTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccounts() {
        // given
        fetchAccountsResponse = mock(FetchAccountsResponse.class);
        accountResourceEntity = mock(AccountResourceEntity.class);
        transactionalAccount = mock(TransactionalAccount.class);

        List<AccountResourceEntity> accountsList = new ArrayList<>();
        accountsList.add(accountResourceEntity);

        when(apiClient.fetchAccounts()).thenReturn(fetchAccountsResponse);
        when(fetchAccountsResponse.getAccounts()).thenReturn(accountsList);
        when(accountResourceEntity.toTinkAccount()).thenReturn(Optional.of(transactionalAccount));

        // when
        Collection<TransactionalAccount> response =
                cmcicTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertTrue(response.contains(transactionalAccount));
    }

    @Test
    public void shouldReturnEmptyCollectionOnFetchAccounts() {
        // given
        List<AccountResourceEntity> accountsList = new ArrayList<>();

        when(apiClient.fetchAccounts()).thenReturn(fetchAccountsResponse);
        when(fetchAccountsResponse.getAccounts()).thenReturn(accountsList);
        when(accountResourceEntity.toTinkAccount()).thenReturn(Optional.of(transactionalAccount));

        // when
        Collection<TransactionalAccount> response =
                cmcicTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(response);
        assertEquals(0, response.size());
    }
}
