package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SocieteGeneraleTransactionFetcherTest {

    @Test
    public void shouldGetTransactions() {
        // given
        SocieteGeneraleApiClient apiClient = mock(SocieteGeneraleApiClient.class);
        SocieteGeneraleConfiguration societeGeneraleConfiguration =
                mock(SocieteGeneraleConfiguration.class);
        SessionStorage sessionStorage = mock(SessionStorage.class);
        SignatureHeaderProvider signatureHeaderProvider = mock(SignatureHeaderProvider.class);
        TransactionalAccount account = mock(TransactionalAccount.class);
        URL nextPageUrl = new URL("someUrl");
        String signature = "signature";
        String token = "token";
        String apiIdentifier = "apiIdentifier";
        TransactionsResponse transactionsResponse = mock(TransactionsResponse.class);
        EidasProxyConfiguration eidasProxyConfiguration = mock(EidasProxyConfiguration.class);
        EidasIdentity eidasIdentity = mock(EidasIdentity.class);

        when(signatureHeaderProvider.buildSignatureHeader(
                        any(), any(), anyString(), anyString(), any()))
                .thenReturn(signature);
        when(sessionStorage.get(SocieteGeneraleConstants.StorageKeys.TOKEN)).thenReturn(token);
        when(account.getApiIdentifier()).thenReturn(apiIdentifier);
        when(apiClient.getTransactions(anyString(), anyString(), anyString(), any()))
                .thenReturn(transactionsResponse);

        SocieteGeneraleTransactionFetcher societeGeneraleTransactionFetcher =
                new SocieteGeneraleTransactionFetcher(
                        apiClient,
                        societeGeneraleConfiguration,
                        sessionStorage,
                        signatureHeaderProvider,
                        eidasProxyConfiguration,
                        eidasIdentity);

        // when
        TransactionKeyPaginatorResponse<URL> response =
                societeGeneraleTransactionFetcher.getTransactionsFor(account, nextPageUrl);

        // then
        assertNotNull(response);
        assertEquals(transactionsResponse, response);
    }
}
