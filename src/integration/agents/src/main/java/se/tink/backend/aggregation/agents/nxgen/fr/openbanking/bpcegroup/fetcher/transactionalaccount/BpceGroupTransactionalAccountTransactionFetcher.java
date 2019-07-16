package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BpceGroupTransactionalAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final BpceGroupApiClient apiClient;

    public BpceGroupTransactionalAccountTransactionFetcher(BpceGroupApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {

        // TODO remove for prod
        if (sandboxFilter(account)) {
            return new TransactionsResponse();
        }

        return Optional.ofNullable(key)
                .map(apiClient::getTransactions)
                .orElseGet(() -> apiClient.getTransactions(account));
    }

    // TODO remove for prod
    private boolean sandboxFilter(TransactionalAccount account) {
        return !account.getIdModule().getUniqueId().equalsIgnoreCase("FR7613807008043001965408359");
    }
}
