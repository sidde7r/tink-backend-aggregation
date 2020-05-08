package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class SocieteGeneraleTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, URL> {

    private final SocieteGeneraleApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SignatureHeaderProvider signatureHeaderProvider;

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            TransactionalAccount account, URL nextPageUrl) {
        String reqId = UUID.randomUUID().toString();
        return apiClient.getTransactions(
                account.getApiIdentifier(), buildSignature(reqId), reqId, nextPageUrl);
    }

    private String buildSignature(String requestId) {
        return signatureHeaderProvider.buildSignatureHeader(
                sessionStorage.get(StorageKeys.TOKEN), requestId);
    }
}
