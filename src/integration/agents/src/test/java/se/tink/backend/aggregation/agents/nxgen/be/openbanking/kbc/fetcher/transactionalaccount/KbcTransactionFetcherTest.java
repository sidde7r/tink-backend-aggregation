package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class KbcTransactionFetcherTest {
    private BerlinGroupApiClient apiClient;
    private BerlinGroupTransactionFetcher fetcher;

    @Before
    public void init() {
        apiClient = mock(BerlinGroupApiClient.class);
        fetcher = new BerlinGroupTransactionFetcher(apiClient);
    }

    @Test
    public void shouldFetchTransactionsAndConvertItToTinkModel() {
        // given
        String url = "url";
        TransactionsKeyPaginatorBaseResponse transactionsKeyPaginatorBaseResponse =
                mock(TransactionsKeyPaginatorBaseResponse.class);
        when(apiClient.fetchTransactions(url)).thenReturn(transactionsKeyPaginatorBaseResponse);
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        // when
        TransactionKeyPaginatorResponse result =
                fetcher.getTransactionsFor(transactionalAccount, url);

        // then
        assertThat(result).isEqualTo(transactionsKeyPaginatorBaseResponse);
        verify(apiClient).fetchTransactions(url);
        verifyNoMoreInteractions(apiClient);
    }
}
