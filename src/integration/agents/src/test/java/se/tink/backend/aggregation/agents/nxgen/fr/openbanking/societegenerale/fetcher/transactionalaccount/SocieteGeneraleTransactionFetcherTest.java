package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SocieteGeneraleTransactionFetcherTest {

    @Test
    public void shouldGetTransactions() {
        // given
        SocieteGeneraleApiClient apiClient = mock(SocieteGeneraleApiClient.class);

        TransactionalAccount account = mock(TransactionalAccount.class);
        URL nextPageUrl = new URL("someUrl");
        String apiIdentifier = "apiIdentifier";
        TransactionsResponse transactionsResponse = mock(TransactionsResponse.class);

        when(account.getApiIdentifier()).thenReturn(apiIdentifier);
        when(apiClient.getTransactions(anyString(), any())).thenReturn(transactionsResponse);

        SocieteGeneraleTransactionFetcher societeGeneraleTransactionFetcher =
                new SocieteGeneraleTransactionFetcher(apiClient);

        // when
        TransactionKeyPaginatorResponse<URL> response =
                societeGeneraleTransactionFetcher.getTransactionsFor(account, nextPageUrl);

        // then
        assertNotNull(response);
        assertEquals(transactionsResponse, response);
    }
}
