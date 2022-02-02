package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getAccountResponseWithConsent;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getAccountResponseWithoutConsent;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getBalancesResponse;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RunWith(MockitoJUnitRunner.class)
public class BredBanquePopulaireTransactionalAccountFetcherTest {

    @Mock private BredBanquePopulaireApiClient apiClient;
    private BredBanquePopulaireTransactionalAccountFetcher transactionalAccountFetcher;

    @Before
    public void setUp() {
        when(apiClient.fetchBalances(RESOURCE_ID)).thenReturn(getBalancesResponse());

        transactionalAccountFetcher = new BredBanquePopulaireTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccountsTwiceWithCallForBalances() {
        // given
        when(apiClient.fetchAccounts()).thenReturn(getAccountResponseWithoutConsent());

        // when
        final Collection<TransactionalAccount> result = transactionalAccountFetcher.fetchAccounts();

        // then
        verify(apiClient, times(2)).fetchAccounts();
        verify(apiClient).recordCustomerConsent(any());
        verify(apiClient).fetchBalances(RESOURCE_ID);
        assertThat(result).hasSize(1);
    }

    @Test
    public void shouldFetchAccountsTwiceWithoutCallForBalances() {
        // given
        when(apiClient.fetchAccounts())
                .thenReturn(getAccountResponseWithoutConsent(), getAccountResponseWithConsent());

        // when
        final Collection<TransactionalAccount> result = transactionalAccountFetcher.fetchAccounts();

        // then
        verify(apiClient, times(2)).fetchAccounts();
        verify(apiClient).recordCustomerConsent(any());
        verify(apiClient, never()).fetchBalances(anyString());
        assertThat(result).hasSize(1);
    }

    @Test
    public void shouldFetchAccountsWithoutRecordingCustomerConsent() {
        // given
        when(apiClient.fetchAccounts()).thenReturn(getAccountResponseWithConsent());

        // when
        final Collection<TransactionalAccount> result = transactionalAccountFetcher.fetchAccounts();

        // then
        verify(apiClient).fetchAccounts();
        verify(apiClient, never()).recordCustomerConsent(any());
        verify(apiClient, never()).fetchBalances(anyString());
        assertThat(result).hasSize(1);
    }

    @Test
    public void shouldNotFetchAnyAccounts() {
        // given
        when(apiClient.fetchAccounts()).thenReturn(AccountsResponse.empty());

        // when
        final Collection<TransactionalAccount> result = transactionalAccountFetcher.fetchAccounts();

        // then
        verify(apiClient).fetchAccounts();
        verify(apiClient, never()).recordCustomerConsent(any());
        verify(apiClient, never()).fetchBalances(anyString());
        assertThat(result).isEmpty();
    }
}
