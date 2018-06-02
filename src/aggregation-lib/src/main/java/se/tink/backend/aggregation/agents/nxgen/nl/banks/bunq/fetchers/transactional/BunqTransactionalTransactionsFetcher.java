package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;

public class BunqTransactionalTransactionsFetcher implements TransactionKeyPaginator<TransactionalAccount, String> {
    private final Credentials credentials;
    private final SessionStorage sessionStorage;
    private final BunqApiClient apiClient;

    public BunqTransactionalTransactionsFetcher(Credentials credentials,
            SessionStorage sessionStorage, BunqApiClient apiClient) {
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(TransactionalAccount account, String key) {
        if (Strings.isNullOrEmpty(key)) {
            return apiClient.listAccountTransactions(
                    sessionStorage.get(BunqConstants.StorageKeys.USER_ID), account.getBankIdentifier());
        }

        return apiClient.listAccountTransactionsPagination(key);
    }
}
