package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BunqTransactionalTransactionsFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {
    private final SessionStorage sessionStorage;
    private final BunqApiClient apiClient;

    public BunqTransactionalTransactionsFetcher(
            SessionStorage sessionStorage, BunqApiClient apiClient) {
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        if (Strings.isNullOrEmpty(key)) {
            return apiClient.listAccountTransactions(
                    sessionStorage.get(BunqConstants.StorageKeys.USER_ID),
                    account.getBankIdentifier());
        }

        return apiClient.listAccountTransactionsPagination(key);
    }
}
