package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SocieteGeneraleSignatureUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SocieteGeneraleTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, URL> {

    private final SocieteGeneraleApiClient apiClient;
    private final EidasProxyConfiguration eidasProxyConfiguration;
    private final SocieteGeneraleConfiguration societeGeneraleConfiguration;
    private final SessionStorage sessionStorage;
    private final EidasIdentity eidasIdentity;

    public SocieteGeneraleTransactionFetcher(
            SocieteGeneraleApiClient apiClient,
            EidasProxyConfiguration eidasProxyConfiguration,
            SocieteGeneraleConfiguration societeGeneraleConfiguration,
            SessionStorage sessionStorage,
            EidasIdentity eidasIdentity) {
        this.apiClient = apiClient;
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.societeGeneraleConfiguration = societeGeneraleConfiguration;
        this.sessionStorage = sessionStorage;
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
        return SocieteGeneraleSignatureUtils.buildSignatureHeader(
                eidasProxyConfiguration,
                eidasIdentity,
                sessionStorage.get(StorageKeys.TOKEN),
                requestId,
                societeGeneraleConfiguration);
    }
}
