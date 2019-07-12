package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SibsTransactionalAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final SibsBaseApiClient apiClient;

    public SibsTransactionalAccountTransactionFetcher(SibsBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return Optional.ofNullable(key)
                .map(apiClient::getTransactionsForKey)
                .orElseGet(() -> apiClient.getAccountTransactions(account));
    }
}
