package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SdcTransactionalAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final SdcApiClient apiClient;

    public SdcTransactionalAccountTransactionFetcher(SdcApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return apiClient.getTransactionsFor(account, key);

        //        return Optional.ofNullable(key)
        //            .map(apiClient::getTransactionsFor)
        //            .orElseGet(() -> apiClient.getTransactionsForAccount(account));
    }
}
