package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class TransactionalAccountFetcherTest {

    private TransactionalAccountFetcher fetcher;
    private IspApiClient apiClient;

    @Before
    public void setup() {
        this.apiClient = mock(IspApiClient.class);
        this.fetcher = new TransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchCheckingAccount() {
        // given
        when(apiClient.fetchAccountsAndIdentities())
                .thenReturn(FetchersTestData.checkingAccountResponse());
        // when
        List<TransactionalAccount> accounts = new ArrayList<>(fetcher.fetchAccounts());
        // then
        assertThat(accounts).hasSize(1);
        TransactionalAccount account = accounts.get(0);
        assertThat(account.getApiIdentifier()).isEqualTo("987654321");
        assertThat(account.getIdentifiers()).hasSize(1);
        assertThat(account.getIdentifiers().get(0).getType()).isEqualTo(AccountIdentifierType.IBAN);
        assertThat(account.getIdentifiers().get(0).getIdentifier())
                .isEqualTo("IT58F0300203280166615394326");
        assertThat(account.getExactBalance().getExactValue()).isEqualByComparingTo("500");
        assertThat(account.getName()).isEqualTo("XME Conto");
    }
}
