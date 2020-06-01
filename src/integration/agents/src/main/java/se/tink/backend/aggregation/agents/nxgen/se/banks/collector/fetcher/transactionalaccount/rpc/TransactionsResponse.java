package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private List<TransactionEntity> transactions;

    public TransactionsResponse(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false);
    }

    @Override
    public String nextKey() {
        return null;
    }
}
