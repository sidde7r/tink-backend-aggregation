package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SocieteGeneraleTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, URL> {

    private final SocieteGeneraleApiClient apiClient;
    private final SocieteGeneraleConfiguration societeGeneraleConfiguration;
    private final SessionStorage sessionStorage;
    private final SignatureHeaderProvider signatureHeaderProvider;
    private final EidasProxyConfiguration eidasProxyConfiguration;
    private final EidasIdentity eidasIdentity;

    public SocieteGeneraleTransactionFetcher(
            SocieteGeneraleApiClient apiClient,
            SocieteGeneraleConfiguration societeGeneraleConfiguration,
            SessionStorage sessionStorage,
            SignatureHeaderProvider signatureHeaderProvider,
            EidasProxyConfiguration eidasProxyConfiguration,
            EidasIdentity eidasIdentity) {
        this.apiClient = apiClient;
        this.societeGeneraleConfiguration = societeGeneraleConfiguration;
        this.sessionStorage = sessionStorage;
        this.signatureHeaderProvider = signatureHeaderProvider;
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.eidasIdentity = eidasIdentity;
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            TransactionalAccount account, URL nextPageUrl) {
        String reqId = UUID.randomUUID().toString();
        return apiClient.getTransactions(
                account.getApiIdentifier(), buildSignature(reqId), reqId, nextPageUrl);
    }

    private String buildSignature(String requestId) {
        return signatureHeaderProvider.buildSignatureHeader(
                eidasProxyConfiguration,
                eidasIdentity,
                sessionStorage.get(StorageKeys.TOKEN),
                requestId,
                societeGeneraleConfiguration);
    }
}
