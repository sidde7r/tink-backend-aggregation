package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.agents.rpc.AccountTypes.CHECKING;
import static se.tink.libraries.amount.ExactCurrencyAmount.inEUR;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ArgentaTransactionalAccountFetcherTest {
    private ArgentaApiClient apiClient;
    private ArgentaTransactionalAccountFetcher fetcher;

    @Before
    public void init() {
        apiClient = mock(ArgentaApiClient.class);
        fetcher = new ArgentaTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccountsAndConvertItToTinkModel() {
        // given
        when(apiClient.getAccounts()).thenReturn(ArgentaFetcherTestData.ACCOUNT_RESPONSE);

        // when
        Collection<TransactionalAccount> result = fetcher.fetchAccounts();

        // then
        TransactionalAccount account = result.iterator().next();
        assertThat(account.getName()).isEqualTo("PRODUCT");
        assertThat(account.getType()).isEqualTo(CHECKING);
        assertThat(account.getAccountNumber()).isEqualTo("BBAN");
        assertThat(account.getApiIdentifier()).isEqualTo("RESOURCE_ID");
        assertThat(account.getExactBalance()).isEqualTo(inEUR(6.66));
        verify(apiClient).getAccounts();
        verifyNoMoreInteractions(apiClient);
    }
}
