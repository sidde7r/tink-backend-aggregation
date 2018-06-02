package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BelfiusTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionDatePaginator<TransactionalAccount> {

    private final BelfiusApiClient apiClient;

    public BelfiusTransactionalAccountFetcher(BelfiusApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return this.apiClient.fetchProducts().stream()
                .filter(entry -> entry.getValue().isTransactionalAccount())
                .map(entry -> entry.getValue().toTransactionalAccount(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Transaction> getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        String key = account.getBankIdentifier();
        return this.apiClient.fetchTransactions(key, fromDate, toDate).stream()
                .map(BelfiusTransaction::toTinkTransaction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
