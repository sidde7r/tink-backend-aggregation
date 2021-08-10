package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts;

import java.time.LocalDate;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountTransactionsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SabadellTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, Boolean> {
    private final SabadellApiClient apiClient;
    private final LocalDate now;

    public SabadellTransactionFetcher(SabadellApiClient apiClient, LocalDate now) {
        this.apiClient = apiClient;
        this.now = now;
    }

    @Override
    public TransactionKeyPaginatorResponse<Boolean> getTransactionsFor(
            TransactionalAccount account, Boolean key) {
        boolean moreRequest = Optional.ofNullable(key).orElse(false);
        return account.getFromTemporaryStorage(account.getAccountNumber(), AccountEntity.class)
                .map(
                        entity ->
                                AccountTransactionsRequest.builder()
                                        .account(entity)
                                        .moreRequest(moreRequest)
                                        .dateFrom(now.minusYears(10))
                                        .dateTo(now)
                                        .build())
                .map(apiClient::fetchTransactions)
                .orElseThrow(() -> new IllegalStateException("No account entity provided"));
    }
}
