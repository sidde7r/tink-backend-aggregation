package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.getTransactionalAccount;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.getTransactionsResponse;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BpceGroupTransactionFetcherTest {

    private BpceGroupTransactionFetcher bpceGroupTransactionFetcher;

    private BpceGroupApiClient bpceGroupApiClientMock;

    @Before
    public void setUp() {
        bpceGroupApiClientMock = mock(BpceGroupApiClient.class);

        final TransactionsResponse transactionsResponse = getTransactionsResponse();
        when(bpceGroupApiClientMock.getTransactions(RESOURCE_ID)).thenReturn(transactionsResponse);
        when(bpceGroupApiClientMock.getTransactions(RESOURCE_ID, 2))
                .thenReturn(transactionsResponse);

        bpceGroupTransactionFetcher = new BpceGroupTransactionFetcher(bpceGroupApiClientMock);
    }

    @Test
    public void shouldGetTransactionsForPage1() {
        // given
        final TransactionalAccount account = getTransactionalAccount();

        // when
        final PaginatorResponse response =
                bpceGroupTransactionFetcher.getTransactionsFor(account, 1);

        // then
        verify(bpceGroupApiClientMock).getTransactions(RESOURCE_ID);
        verify(bpceGroupApiClientMock, never()).getTransactions(anyString(), anyInt());
        verify(bpceGroupApiClientMock, never()).recordCustomerConsent(any());
        assertThat(response).isNotNull();
        assertThat(response.getTinkTransactions()).hasSize(3);
    }

    @Test
    public void shouldGetTransactionsForPage2() {
        // given
        final TransactionalAccount account = getTransactionalAccount();

        // when
        final PaginatorResponse response =
                bpceGroupTransactionFetcher.getTransactionsFor(account, 2);

        // then
        verify(bpceGroupApiClientMock, never()).getTransactions(RESOURCE_ID);
        verify(bpceGroupApiClientMock).getTransactions(RESOURCE_ID, 2);
        verify(bpceGroupApiClientMock, never()).recordCustomerConsent(any());
        assertThat(response).isNotNull();
        assertThat(response.getTinkTransactions()).hasSize(3);
    }
}
