package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher.ArkeaFetcherFixtures.*;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient.ArkeaApiClient;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RunWith(MockitoJUnitRunner.class)
public class ArkeaTransactionalAccountFetcherTest {
    @Mock private ArkeaApiClient apiClient;

    private ArkeaTransactionalAccountFetcher transactionalAccountFetcher;

    @Before
    public void setUp() {
        transactionalAccountFetcher = new ArkeaTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchTransactionalAccounts() {
        // given
        when(apiClient.getAccounts()).thenReturn(TRANSACTIONAL_ACCOUNTS_RESPONSE);
        when(apiClient.getBalances(any())).thenReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts).isNotEmpty();
        assertThat(transactionalAccounts).hasSize(1);
        TransactionalAccount transactionalAccount = transactionalAccounts.iterator().next();
        assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(transactionalAccount.getName()).isEqualTo("Compte de Mr et Mme Dupont");
        assertThat(transactionalAccount.getAccountNumber()).isEqualTo("FR1212341234123412");
        assertThat(transactionalAccount.getExactBalance().getDoubleValue()).isEqualTo(123.45);
        assertThat(transactionalAccount.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
    }

    @Test
    public void shouldReturnEmptyListOfAccountsWhenApiReturnsNoAccounts() {
        // given
        when(apiClient.getAccounts()).thenReturn(NO_TRANSACTIONAL_ACCOUNTS_RESPONSE);

        // when
        Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldTrowWhenApiReturnsNoBalances() {
        // given
        when(apiClient.getAccounts()).thenReturn(TRANSACTIONAL_ACCOUNTS_RESPONSE);
        when(apiClient.getBalances(any())).thenReturn(NO_BALANCE_RESPONSE);

        // when
        transactionalAccountFetcher.fetchAccounts();
    }
}
