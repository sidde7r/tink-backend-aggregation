package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BerlinGroupAccountResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class KbcAccountFetcherTest {
    private BerlinGroupApiClient apiClient;
    private BerlinGroupAccountFetcher fetcher;

    @Before
    public void init() {
        apiClient = mock(BerlinGroupApiClient.class);
        fetcher = new BerlinGroupAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccountsAndConvertItToTinkModel() {
        // given
        BerlinGroupAccountResponse berlinGroupAccountResponse =
                mock(BerlinGroupAccountResponse.class);
        Collection<TransactionalAccount> accountsTinkModel = Lists.emptyList();
        when(berlinGroupAccountResponse.toTinkAccounts()).thenReturn(accountsTinkModel);
        when(apiClient.fetchAccounts()).thenReturn(berlinGroupAccountResponse);

        // when
        Collection<TransactionalAccount> result = fetcher.fetchAccounts();

        // then
        assertThat(result).isEqualTo(accountsTinkModel);
        verify(apiClient).fetchAccounts();
        verifyNoMoreInteractions(apiClient);
    }
}
