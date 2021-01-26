package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.getCreditCardAccount;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.getTransactionsResponse;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class BpceGroupCardTransactionsFetcherTest {

    private BpceGroupCardTransactionsFetcher fetcher;

    private BpceGroupApiClient apiClient;

    @Before
    public void setUp() {
        apiClient = mock(BpceGroupApiClient.class);

        final TransactionsResponse transactionsResponse = getTransactionsResponse();
        when(apiClient.getTransactions(RESOURCE_ID)).thenReturn(transactionsResponse);
        when(apiClient.getTransactions(RESOURCE_ID, 2)).thenReturn(transactionsResponse);

        fetcher = new BpceGroupCardTransactionsFetcher(apiClient);
    }

    @Test
    public void shouldGetTransactionsForPage1() {
        // given
        final CreditCardAccount account = getCreditCardAccount();

        // when
        final PaginatorResponse response = fetcher.getTransactionsFor(account, 1);

        // then
        verify(apiClient).getTransactions(RESOURCE_ID);
        verify(apiClient, never()).getTransactions(anyString(), anyInt());
        verify(apiClient, never()).recordCustomerConsent(any());
        assertThat(response).isNotNull();
        assertThat(response.getTinkTransactions()).hasSize(3);
    }

    @Test
    public void shouldGetTransactionsForPage2() {
        // given
        final CreditCardAccount account = getCreditCardAccount();

        // when
        final PaginatorResponse response = fetcher.getTransactionsFor(account, 2);

        // then
        verify(apiClient, never()).getTransactions(RESOURCE_ID);
        verify(apiClient).getTransactions(RESOURCE_ID, 2);
        verify(apiClient, never()).recordCustomerConsent(any());
        assertThat(response).isNotNull();
        assertThat(response.getTinkTransactions()).hasSize(3);
    }
}
