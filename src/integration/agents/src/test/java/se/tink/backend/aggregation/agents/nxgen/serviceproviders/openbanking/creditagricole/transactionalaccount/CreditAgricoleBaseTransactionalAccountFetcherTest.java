package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CreditAgricoleBaseTransactionalAccountFetcherTest {

    @Test
    public void shouldFetchAccountsWithNoNecessaryConsents() {
        // given
        CreditAgricoleBaseApiClient apiClient = mock(CreditAgricoleBaseApiClient.class);
        GetAccountsResponse accountsResponse = mock(GetAccountsResponse.class);
        Collection<TransactionalAccount> transactionalAccounts = mock(Collection.class);

        CreditAgricoleBaseTransactionalAccountFetcher
                creditAgricoleBaseTransactionalAccountFetcher =
                        new CreditAgricoleBaseTransactionalAccountFetcher(apiClient);

        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        when(accountsResponse.areConsentsNecessary()).thenReturn(false);
        when(accountsResponse.toTinkAccounts()).thenReturn(transactionalAccounts);

        // when
        Collection<TransactionalAccount> resp =
                creditAgricoleBaseTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(transactionalAccounts, resp);
        verify(apiClient).getAccounts();
    }

    @Test
    public void shouldFetchAccountsWithNecessaryConsents() {
        // given
        CreditAgricoleBaseApiClient apiClient = mock(CreditAgricoleBaseApiClient.class);
        GetAccountsResponse accountsResponse = mock(GetAccountsResponse.class);
        Collection<TransactionalAccount> transactionalAccounts = mock(Collection.class);
        List<AccountIdEntity> listOfNecessaryConstents = mock(List.class);

        CreditAgricoleBaseTransactionalAccountFetcher
                creditAgricoleBaseTransactionalAccountFetcher =
                        new CreditAgricoleBaseTransactionalAccountFetcher(apiClient);

        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        when(accountsResponse.areConsentsNecessary()).thenReturn(true);
        when(accountsResponse.toTinkAccounts()).thenReturn(transactionalAccounts);
        when(accountsResponse.getListOfNecessaryConsents()).thenReturn(listOfNecessaryConstents);

        doNothing().when(apiClient).putConsents(listOfNecessaryConstents);

        // when
        Collection<TransactionalAccount> resp =
                creditAgricoleBaseTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(transactionalAccounts, resp);
        verify(apiClient, times(2)).getAccounts();
    }
}
