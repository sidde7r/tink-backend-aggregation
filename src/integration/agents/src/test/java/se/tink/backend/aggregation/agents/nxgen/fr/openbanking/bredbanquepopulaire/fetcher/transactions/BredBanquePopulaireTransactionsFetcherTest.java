package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getTransactionalAccount;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getTransactionsResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons.BredBanquePopulaireTransactionsFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RunWith(MockitoJUnitRunner.class)
public class BredBanquePopulaireTransactionsFetcherTest {
    @Mock private BredBanquePopulaireApiClient apiClient;
    private BredBanquePopulaireTransactionsFetcher<TransactionalAccount> transactionsFetcher;

    @Before
    public void setUp() {
        when(apiClient.getTransactions(RESOURCE_ID)).thenReturn(getTransactionsResponse(true));
        when(apiClient.getTransactions(RESOURCE_ID, 1)).thenReturn(getTransactionsResponse(false));

        transactionsFetcher = new BredBanquePopulaireTransactionsFetcher<>(apiClient);
    }

    @Test
    public void shouldGetFirstPageOfTransactions() {
        // given
        final TransactionalAccount account = getTransactionalAccount();

        // when
        final PaginatorResponse response = transactionsFetcher.getTransactionsFor(account, 0);

        // then
        verify(apiClient).getTransactions(RESOURCE_ID);
        verify(apiClient, never()).getTransactions(anyString(), anyInt());
        assertThat(response).isNotNull();
        assertThat(response.getTinkTransactions()).hasSize(3);
        assertThat(response.canFetchMore().get()).isTrue();
    }

    @Test
    public void shouldGetSecondPageOfTransactions() {
        // given
        final TransactionalAccount account = getTransactionalAccount();

        // when
        final PaginatorResponse response = transactionsFetcher.getTransactionsFor(account, 1);

        // then
        verify(apiClient, never()).getTransactions(RESOURCE_ID);
        verify(apiClient).getTransactions(RESOURCE_ID, 1);
        assertThat(response).isNotNull();
        assertThat(response.getTinkTransactions()).hasSize(3);
        assertThat(response.canFetchMore().get()).isFalse();
    }
}
